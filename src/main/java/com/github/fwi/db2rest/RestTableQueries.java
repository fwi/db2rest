package com.github.fwi.db2rest;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestTableQueries {

	private static final Logger log = LoggerFactory.getLogger(RestTableQueries.class);

	public static final String DB_QUERY_PREFIX = "rest2db_query_";
	// public static final String DB_QUERY_ORDER = DB_QUERY_PREFIX + "order";
	public static final String DB_QUERY_FILTERS = DB_QUERY_PREFIX + "filters";
	public static final String FILTER_COLUMN = "column";
	public static final String FILTER_OP = "op";
	public static final String FILTER_VALUE = "value";

	public final String schema;
	public final String tableName;
	public final int maxAmountDefault;
	public final Collection<String> columnNames;
	public final Collection<String> selectOnlyColumns;
	public final Collection<String> timestampColumns;
	public final Map<String, Object> insertDefaults;

	public final JdbcTemplate jdbcTemplate;
	public final NamedParameterJdbcTemplate namedJdbcTemplate;
	public final TransactionTemplate transactionTemplate;

	public final ObjectMapper objectMapper;

	public RestTableQueries(RestTableMeta meta, RestDbResources db, ObjectMapper objectMapper) {
		this.schema = meta.schema;
		this.tableName = meta.tableName;
		this.maxAmountDefault = meta.maxAmountDefault;
		this.columnNames = meta.columnNames;
		this.selectOnlyColumns = meta.selectOnlyColumns;
		this.timestampColumns = meta.timestampColumns;
		this.insertDefaults = meta.insertDefaults;

		this.jdbcTemplate = db.jdbcTemplate;
		this.namedJdbcTemplate = db.namedJdbcTemplate;
		this.transactionTemplate = db.transactionTemplate;
		this.objectMapper = objectMapper;
	}

	public List<Map<String, Object>> insert(List<Map<String, Object>> records) {

		// There is no reliable way to get generated values back when using a
		// batch-update.
		// Use a transaction to ensure an "all or nothing" operation.

		transactionTemplate.execute((status) -> {
			for (var params : records) {
				for (var columnName : insertDefaults.keySet()) {
					params.putIfAbsent(columnName, insertDefaults.get(columnName));
				}
				var keyHolder = new GeneratedKeyHolder();
				var sqlParams = new MapSqlParameterSource().addValues(params);
				var query = insertQuery(new LinkedList<String>(params.keySet()));
				namedJdbcTemplate.update(query, sqlParams, keyHolder);
				if (keyHolder.getKeys() != null) {
					params.putAll(keyHolder.getKeys());
				}
			}
			return null;
		});
		return records;
	}

	public String insertQuery(List<String> columns) {

		// Names do not have to be quoted but be aware of the note in schema-h2.sql
		// Use quotes for names anyway in case a name is a SQL-reserved word (e.g.
		// "user").
		vcolumns(columns);
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(quotedTable()).append(" (");
		for (var it = columns.iterator(); it.hasNext();) {
			var column = it.next();
			if (selectOnlyColumns.contains(column)) {
				throw new BadRequestException("Cannot insert value for select-only column " + column + ".");
			}
			sb.append(quote(column));
			if (it.hasNext()) {
				sb.append(',');
			} else {
				sb.append(')');
			}
		}
		sb.append(" values (");
		for (var it = columns.iterator(); it.hasNext();) {
			sb.append(':').append(it.next());
			if (it.hasNext()) {
				sb.append(',');
			} else {
				sb.append(')');
			}
		}
		return sb.toString();
	}

	public String quotedTable() {
		return (schema == null ? quote(tableName) : quote(schema) + "." + quote(tableName));
	}

	/**
	 * Verify column names to prevent sql-injection.
	 */
	public String vcolumn(String columnName) {

		if (!columnNames.contains(columnName)) {
			throw new BadRequestException("Unknown column name [" + columnName + "] for table " + tableName);
		}
		return columnName;
	}

	/**
	 * Verify column names to prevent sql-injection.
	 */
	public <T extends Collection<String>> T vcolumns(T columnNames) {

		if (!this.columnNames.containsAll(columnNames)) {
			columnNames.removeAll(this.columnNames);
			throw new BadRequestException("Unknown column name in collection " + columnNames);
		}
		return columnNames;
	}

	public List<Map<String, Object>> selectAll() {
		return select(null);
	}

	public List<Map<String, Object>> select(List<Map<String, Object>> records) {
		return select(records, 0, maxAmountDefault);
	}

	public List<Map<String, Object>> select(List<Map<String, Object>> records, int offset, int amount) {

		if (records == null) {
			records = Collections.singletonList(Collections.emptyMap());
		}
		if (amount <= 0) {
			amount = maxAmountDefault;
		}
		var selected = new LinkedList<Map<String, Object>>();
		for (var params : records) {
			var query = limit(selectQuery(params), offset, amount);
			selected.addAll(namedJdbcTemplate.queryForList(query, params));
		}
		return selected;
	}

	public String selectQuery(Map<String, Object> params) {
		return selectQuery(params.keySet(), params);
	}

	public String selectQuery(Collection<String> selectionKeys, Map<String, Object> params) {

		StringBuilder sb = new StringBuilder("select * from ");
		sb.append(quotedTable()).append(where(selectionKeys, params));
		return sb.toString();
	}

	public String quote(String s) {
		return '"' + s + '"';
	}

	protected final String WHERE_SQL_START = " where ";

	public String where(Collection<String> selectionKeys, Map<String, Object> params) {

		/*
		 * This method is used by several query methods.
		 */
		if (selectionKeys.isEmpty()) {
			return StringUtils.EMPTY;
		}
		StringBuilder sb = new StringBuilder(WHERE_SQL_START);
		boolean first = true;
		for (var column : selectionKeys) {
			if (!params.containsKey(column)) {
				continue;
			}
			if (column.startsWith(DB_QUERY_PREFIX)) {
				if (column.equals(DB_QUERY_FILTERS)) {
					String whereFilter = whereFilters(params.get(column), params);
					sb.append(whereFilter);
					if (first) {
						first = StringUtils.isNotBlank(whereFilter);
					}
				}
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append(" and ");
			}
			sb.append(quote(vcolumn(column)));
			if (params.get(column) instanceof Collection) {
				sb.append(" in ( :").append(column).append(')');
			} else {
				sb.append("= :").append(column);
				if (timestampColumns.contains(column)) {
					params.put(column, toTimestamp((String) params.get(column)));
				}
			}
		}
		String whereClause = sb.toString();
		return (whereClause.equals(WHERE_SQL_START) ? StringUtils.EMPTY : whereClause);
	}

	public OffsetDateTime toTimestamp(String value) {

		if (value == null) {
			return null;
		}
		OffsetDateTime t = null;
		try {
			t = objectMapper.readValue(quote(value), OffsetDateTime.class);
		} catch (Exception e) {
			throw new BadRequestException("Invalid date format for value [" + value + "].", e);
		}
		return t;
	}

	public String whereFilters(Object filtersObject, Map<String, Object> params) {

		List<Map<String, Object>> filters = null;
		try {
			@SuppressWarnings("unchecked")
			var filtersMap = (List<Map<String, Object>>) filtersObject;
			filters = filtersMap;
		} catch (Exception e) {
			throw new BadRequestException("Invalid " + DB_QUERY_FILTERS + " format.", e);
		}
		StringBuilder sb = new StringBuilder(StringUtils.EMPTY);
		for (int f = 0; f < filters.size(); f++) {
			var filter = filters.get(f);
			var column = (String) filter.get(FILTER_COLUMN);
			var filterColumnName = column + "_filter_" + f;
			var op = (String) filter.get(FILTER_OP);
			params.put(filterColumnName, filter.get(FILTER_VALUE));
			sb.append(quote(vcolumn(column)));
			switch (op) {
			case "=":
			case ">":
			case ">=":
			case "<":
			case "<=":
				sb.append(' ').append(op).append(" :").append(filterColumnName);
				if (timestampColumns.contains(column)) {
					params.put(filterColumnName, toTimestamp((String) params.get(filterColumnName)));
				}
				break;
			case "in":
				sb.append(" in ( :").append(filterColumnName).append(')');
				break;
			case "like":
			case "ilike":
				// literal % characters should be placed in brackets, e.g. '%75[%]%'
				sb.append(' ').append(op).append(" :").append(filterColumnName);
				break;
			case "is null":
			case "is not null":
				sb.append(' ').append(op);
				params.remove(filterColumnName); // value is ignored in query.
				break;
			default:
				throw new BadRequestException("Unknown op " + op + " in filter.");
			}
			if (f < filters.size() - 1) {
				sb.append(" and ");
			}
		}
		return sb.toString();
	}

	public String limit(String query, int offset, int amount) {
		// "limit 0,1000" does not work for postgres.
		// use longer version "limit 1000 offset 0".
		return query + " limit " + amount + " offset " + offset;
	}

	public int update(List<Map<String, Object>> records) {
		return update(records, false);
	}

	public int update(List<Map<String, Object>> records, boolean allowUpdateAll) {

		int updatedRows = transactionTemplate.execute((status) -> {
			var updated = 0;
			for (var params : records) {
				var query = updateQuery(params, allowUpdateAll);
				updated += namedJdbcTemplate.update(query, params);
			}
			return updated;
		});
		// Note: re-using the records map to select updated records is not an option.
		// The where-filters have scrambled the records.
		return updatedRows;
	}

	public String updateQuery(Map<String, Object> params, boolean allowUpdateAll) {

		StringBuilder sb = new StringBuilder("update ");
		sb.append(quotedTable()).append(" set ");
		boolean first = true;
		for (var column : params.keySet()) {
			if (selectOnlyColumns.contains(column) || column.startsWith(DB_QUERY_PREFIX)) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(quote(vcolumn(column))).append("= :").append(column);
		}
		String whereQuery = null;
		if (params.containsKey(DB_QUERY_FILTERS)) {
			whereQuery = whereFilters(params.get(DB_QUERY_FILTERS), params);
			if (!StringUtils.isBlank(whereQuery)) {
				sb.append(WHERE_SQL_START);
			}
		} else {
			whereQuery = where(selectOnlyColumns, params);
		}
		if (!allowUpdateAll && StringUtils.isBlank(whereQuery)) {
			throw new BadRequestException("Update query requires selection values for a where-clause.");
		}
		sb.append(whereQuery);
		return sb.toString();
	}

	public int deleteAll() {
		var deletedRows = jdbcTemplate.update("delete from " + quotedTable());
		return deletedRows;
	}

	public int delete(List<Map<String, Object>> records) {

		int deletedRows = transactionTemplate.execute((status) -> {
			var deleted = 0;
			for (var params : records) {
				if (params.isEmpty()) {
					throw new BadRequestException(
							"Selection to delete is required (no selection parameters provided).");
				}
				var query = deleteQuery(params);
				deleted += namedJdbcTemplate.update(query, params);
			}
			return deleted;
		});
		return deletedRows;
	}

	public String deleteQuery(Map<String, Object> params) {

		StringBuilder sb = new StringBuilder("delete from ");
		sb.append(quotedTable());
		String whereQuery = where(params.keySet(), params);
		if (StringUtils.isBlank(whereQuery)) {
			throw new BadRequestException("Delete query requires selection values for a where-clause.");
		}
		sb.append(whereQuery);
		return sb.toString();
	}

	public Map<String, Object> meta() {

		// Follow pattern from JdbcTemplate method execute(StatementCallback<T> action)
		// to re-use existing connection.
		var meta = new LinkedHashMap<String, Object>();
		meta.put("columnnames", columnNames);
		meta.put("readonlycolumns", selectOnlyColumns);
		meta.put("columndefaults", insertDefaults);
		meta.put("timestampcolumns", timestampColumns);
		String conSchema = null;
		boolean conSchemaUpdated = false;
		var ds = jdbcTemplate.getDataSource();
		var con = DataSourceUtils.getConnection(ds);
		try {
			meta.put("catalog", con.getCatalog());
			conSchema = con.getSchema();
			if (schema != null && !schema.equals(conSchema)) {
				con.setSchema(schema);
				conSchemaUpdated = true;
			}
			meta.put("schema", schema);
			var metaColumns = new LinkedList<Map<String, Object>>();
			var metaData = con.getMetaData();
			meta.put("username", metaData.getUserName());
			meta.put("columns", metaColumns);
			try (var columns = metaData.getColumns(null, schema, tableName, "%")) {
				var rowMeta = columns.getMetaData();
				while (columns.next()) {
					var column = new HashMap<String, Object>();
					for (int i = 1; i <= rowMeta.getColumnCount(); i++) {
						column.put(rowMeta.getColumnName(i), columns.getObject(i));
					}
					metaColumns.add(column);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conSchemaUpdated) {
				try {
					con.setSchema(conSchema);
				} catch (Exception e) {
					log.warn("Failed to reset connection schema back from " + schema + " to " + conSchema);
				}
			}
			DataSourceUtils.releaseConnection(con, ds);
		}
		return meta;
	}

}

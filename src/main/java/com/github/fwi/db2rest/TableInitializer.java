package com.github.fwi.db2rest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class TableInitializer implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(TableInitializer.class);

	protected final TableQueries tableQueries;
	protected final boolean queryColumnsAtStartup;

	public TableInitializer(TableQueries tableQueries) {
		this(tableQueries, true);
	}

	public TableInitializer(TableQueries tableQueries, boolean queryColumnsAtStartup) {
		this.tableQueries = tableQueries;
		this.queryColumnsAtStartup = queryColumnsAtStartup;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!queryColumnsAtStartup) {
			return;
		}
		var columnNames = tableQueries.meta.columnNames();
		var queryColumnNames = columnNames.isEmpty();
		var timestampColumns = tableQueries.meta.timestampColumns();
		var queryTimestamps = timestampColumns.isEmpty();
		if (queryColumnNames || queryTimestamps) {
			@SuppressWarnings("unchecked")
			var columns = (List<Map<String, Object>>) tableQueries.metaData().get("columns");
			for (var column : columns) {
				// some databases return values in all uppercase, others in all lowercase.
				column = upperCaseKeys(column);
				if (queryColumnNames) {
					columnNames.add((String) column.get("COLUMN_NAME"));
				}
				if (queryTimestamps && isTimestampTypeColumn((String) column.get("TYPE_NAME"))) {
					timestampColumns.add((String) column.get("COLUMN_NAME"));
				}
			}
			if (queryColumnNames) {
				if (columnNames.size() > 0) {
					log.debug("Found {} columns for table {}. Columns: {}", columnNames.size(),
						tableQueries.quotedTable(),
						columnNames);
				} else {
					throw new RuntimeException("Found no columns for table " + tableQueries.quotedTable());
				}
			}
			if (queryTimestamps && timestampColumns.size() > 0) {
				log.debug("Found {} timestamp columns for table {}. Columns: {}", timestampColumns.size(),
					tableQueries.quotedTable(), timestampColumns);
			}
		}
	}
	
	public boolean isTimestampTypeColumn(String typeName) {
	
		if (StringUtils.isBlank(typeName)) {
			return false;
		}
		var tname = typeName.toLowerCase(Locale.ENGLISH);
		// postgresql uses "timestamptz" for timestamp with timezone.
		return "timestamp".equals(tname)
			|| "timestamp without time zone".equals(tname)
			|| "timestamptz".equals(tname)
			|| "timestamp with time zone".equals(tname);
	}

	public Map<String, Object> upperCaseKeys(Map<String, Object> m) {

		var mupper = new HashMap<String, Object>();
		for (String key : m.keySet()) {
			mupper.put(key.toUpperCase(Locale.ENGLISH), m.get(key));
		}
		return mupper;
	}

}

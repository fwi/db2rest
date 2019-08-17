package com.github.fwi.db2rest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TableMeta implements TableMetaInterface {

	private String schema;
	private String tableName;
	private ObjectMapper mapper;
	private int maxAmountDefault;
	private int maxAmountAbsolute;
	private Collection<String> columnNames;
	private Collection<String> selectOnlyColumns;
	private Collection<String> timestampColumns;
	private Map<String, Object> insertDefaults;

	public TableMeta() {
	}

	@Override
	public String schema() {
		return schema;
	}

	@Override
	public String tableName() {
		return tableName;
	}

	@Override
	public Collection<String> columnNames() {
		return columnNames;
	}

	@Override
	public Collection<String> selectOnlyColumns() {
		return selectOnlyColumns;
	}

	@Override
	public Collection<String> timestampColumns() {
		return timestampColumns;
	}

	@Override
	public Map<String, Object> insertDefaults() {
		return insertDefaults;
	}

	@Override
	public int maxAmountDefault() {
		return maxAmountDefault;
	}

	@Override
	public int maxAmountAbsolute() {
		return maxAmountAbsolute;
	}

	@Override
	public ObjectMapper mapper() {
		return mapper;
	}

	@Override
	public OffsetDateTime toTimestamp(String value) {

		if (value == null) {
			return null;
		}
		OffsetDateTime t = null;
		try {
			t = mapper.readValue("\"" + value + "\"", OffsetDateTime.class);
		} catch (Exception e) {
			throw new BadRequestException("Invalid date format for value [" + value + "].", e);
		}
		return t;
	}

	public static TableMeta.Builder builder(String tableName) {
		return builder(null, tableName);
	}

	public static TableMeta.Builder builder(String tableName, ObjectMapper mapper) {
		return builder(null, tableName, mapper);
	}

	public static TableMeta.Builder builder(String schema, String tableName) {
		return builder(schema, tableName, null);
	}

	public static TableMeta.Builder builder(String schema, String tableName, ObjectMapper mapper) {
		return new TableMeta.Builder(schema, tableName, mapper);
	}

	public static class Builder {

		public String schema;
		public String tableName;
		public Collection<String> columnNames = new HashSet<String>();
		public Collection<String> selectOnlyColumns = new HashSet<String>();
		public Collection<String> timestampColumns = new HashSet<String>();
		public Map<String, Object> insertDefaults = new HashMap<String, Object>();
		public int maxAmountDefault = 1_000;
		public int maxAmountAbsolute = 10_000;
		public ObjectMapper mapper;

		public Builder(String tableName) {
			this(null, tableName);
		}

		public Builder(String tableName, ObjectMapper mapper) {
			this(null, tableName, mapper);
		}

		public Builder(String schema, String tableName) {
			this(schema, tableName, null);
		}

		public Builder(String schema, String tableName, ObjectMapper mapper) {
			this.schema = schema;
			this.tableName = tableName;
			this.mapper = mapper;
		}

		public Builder columnName(String columnName) {
			columnNames.add(columnName);
			return this;
		}

		public Builder columnNames(String... columnNames) {
			Arrays.stream(columnNames).forEach(e -> this.columnNames.add(e));
			return this;
		}

		public Builder selectOnlyColumn(String columnName) {
			selectOnlyColumns.add(columnName);
			return this;
		}

		public Builder selectOnlyColumns(String... columnNames) {
			Arrays.stream(columnNames).forEach(e -> selectOnlyColumns.add(e));
			return this;
		}

		public Builder timestampColumn(String columnName) {
			timestampColumns.add(columnName);
			return this;
		}

		public Builder timestampColumns(String... columnNames) {
			Arrays.stream(columnNames).forEach(e -> timestampColumns.add(e));
			return this;
		}

		public Builder insertDefault(String columnName, Object value) {
			insertDefaults.put(columnName, value);
			return this;
		}

		public Builder mapper(ObjectMapper mapper) {
			this.mapper = mapper;
			return this;
		}
		
		public Builder maxAmountDefault(int maxAmountDefault) {
			this.maxAmountDefault = maxAmountDefault;
			return this;
		}

		public Builder maxAmountAbsolute(int maxAmountAbsolute) {
			this.maxAmountAbsolute = maxAmountAbsolute;
			return this;
		}

		public TableMeta build() {

			TableMeta meta = new TableMeta();
			meta.schema = schema;
			meta.tableName = tableName;
			meta.mapper = mapper;
			meta.maxAmountDefault = maxAmountDefault;
			meta.maxAmountAbsolute = maxAmountAbsolute;
			meta.columnNames = columnNames;
			meta.selectOnlyColumns = selectOnlyColumns;
			meta.timestampColumns = timestampColumns;
			meta.insertDefaults = insertDefaults;
			return meta;
		}
	}
}

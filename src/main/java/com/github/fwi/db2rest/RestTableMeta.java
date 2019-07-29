package com.github.fwi.db2rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RestTableMeta {

	public String schema;
	public String tableName;
	public Collection<String> columnNames;
	public Collection<String> selectOnlyColumns;
	public Collection<String> timestampColumns;
	public Map<String, Object> insertDefaults;
	public int maxAmountDefault;

	public RestTableMeta() {
	}

	public static RestTableMeta.Builder builder(String tableName) {
		return builder(null, tableName);
	}

	public static RestTableMeta.Builder builder(String schema, String tableName) {
		return new RestTableMeta.Builder(schema, tableName);
	}

	public static class Builder {

		public String schema;
		public String tableName;
		public Collection<String> columnNames  = new HashSet<String>();
		public Collection<String> selectOnlyColumns = new HashSet<String>();
		public Collection<String> timestampColumns = new HashSet<String>();
		public Map<String, Object> insertDefaults = new HashMap<String, Object>();
		public int maxAmountDefault = 1000;

		public Builder(String tableName) {
			this(null, tableName);
		}

		public Builder(String schema, String tableName) {
			this.schema = schema;
			this.tableName = tableName;
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

		public RestTableMeta build() {

			RestTableMeta meta = new RestTableMeta();
			meta.schema = schema;
			meta.tableName = tableName;
			meta.maxAmountDefault = maxAmountDefault;
			meta.columnNames = columnNames;
			meta.selectOnlyColumns = selectOnlyColumns;
			meta.timestampColumns = timestampColumns;
			meta.insertDefaults = insertDefaults;
			return meta;
		}
	}
}

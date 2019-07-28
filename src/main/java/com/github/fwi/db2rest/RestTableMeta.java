package com.github.fwi.db2rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RestTableMeta {

	public String tableName;
	public Collection<String> columnNames;
	public Collection<String> selectOnlyColumns;
	public Collection<String> timestampColumns;
	public Map<String, Object> insertDefaults;
	public int maxAmountDefault;

	public RestTableMeta() {
	}

	public static RestTableMeta.Builder builder(String tableName) {
		return new RestTableMeta.Builder(tableName);
	}

	public static class Builder {

		public String tableName;
		public Collection<String> columnNames  = new HashSet<String>();
		public Collection<String> selectOnlyColumns = new HashSet<String>();
		public Collection<String> timestampColumns = new HashSet<String>();
		public Map<String, Object> insertDefaults = new HashMap<String, Object>();
		public int maxAmountDefault = 1000;

		public Builder(String tableName) {
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

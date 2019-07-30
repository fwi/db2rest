package com.github.fwi.db2rest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class RestTableInitializer implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(RestTableInitializer.class);

	protected final RestTableQueries tableQueries;
	protected final boolean queryColumnsAtStartup;

	public RestTableInitializer(RestTableQueries tableQueries) {
		this(tableQueries, true);
	}

	public RestTableInitializer(RestTableQueries tableQueries, boolean queryColumnsAtStartup) {
		this.tableQueries = tableQueries;
		this.queryColumnsAtStartup = queryColumnsAtStartup;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!queryColumnsAtStartup) {
			return;
		}
		var columnNames = tableQueries.columnNames;
		var queryColumnNames = columnNames.isEmpty();
		var timestampColumns = tableQueries.timestampColumns;
		var queryTimestamps = timestampColumns.isEmpty();
		if (queryColumnNames || queryTimestamps) {
			@SuppressWarnings("unchecked")
			var columns = (List<Map<String, Object>>) tableQueries.meta().get("columns");
			for (var column : columns) {
				// some databases return values in all uppercase, others in all lowercase.
				column = upperCaseKeys(column);
				if (queryColumnNames) {
					columnNames.add((String) column.get("COLUMN_NAME"));
				}
				if (queryTimestamps && ((String) column.get("TYPE_NAME")).equalsIgnoreCase("TIMESTAMP")) {
					timestampColumns.add((String) column.get("COLUMN_NAME"));
				}
			}
			if (queryColumnNames) {
				if (columnNames.size() > 0) {
					log.debug("Found {} columns for table {}. Columns: {}", columnNames.size(), tableQueries.quotedTable(),
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

	
	public Map<String, Object> upperCaseKeys(Map <String, Object> m) {
		
		var mupper = new HashMap<String, Object>();
		for (String key : m.keySet()) {
			mupper.put(key.toUpperCase(Locale.US), m.get(key));
		}
		return mupper;
	}

}

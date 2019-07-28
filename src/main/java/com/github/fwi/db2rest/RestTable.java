package com.github.fwi.db2rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public class RestTable implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(RestTable.class);

	public final RestTableQueries tableQueries;
	protected final boolean queryColumnsAtStartup;

	public RestTable(RestTableQueries tableQueries) {
		this(tableQueries, true);
	}

	public RestTable(RestTableQueries tableQueries, boolean queryColumnsAtStartup) {
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
			var meta = (Map<String, Object>) meta();
			@SuppressWarnings("unchecked")
			var columns = (List<Map<String, Object>>) meta.get("columns");
			for (var column : columns) {
				if (queryColumnNames) {
					columnNames.add((String) column.get("COLUMN_NAME"));
				}
				if (queryTimestamps && ((String) column.get("TYPE_NAME")).equalsIgnoreCase("TIMESTAMP")) {
					timestampColumns.add((String) column.get("COLUMN_NAME"));
				}
			}
			if (queryColumnNames) {
				if (columnNames.size() > 0) {
					log.debug("Found {} columns for table {}. Columns: {}", columnNames.size(),
							tableQueries.tableName, columnNames);
				} else {
					log.warn("Found no columns for table {}.", tableQueries.tableName);
				}
			}
			if (queryTimestamps) {
				if (timestampColumns.size() > 0) {
					log.debug("Found {} timestamp columns for table {}. Columns: {}", timestampColumns.size(),
							tableQueries.tableName, timestampColumns);
				} else {
					log.debug("Found no timestamp columns for table {}.", tableQueries.tableName);
				}
			}
		}
	}

	@GetMapping("/meta")
	public Map<String, Object> meta() {
		return tableQueries.meta();
	}

	@GetMapping("/select/all")
	public List<Map<String, Object>> selectAll() {
		return tableQueries.selectAll();
	}

	/*
	 * GET with a body is not always supported, allow POST as well.
	 */
	@RequestMapping(value = "/select", method = { RequestMethod.GET, RequestMethod.POST })
	public List<Map<String, Object>> select(@RequestBody(required = false) List<Map<String, Object>> records,
			@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "0") int amount) {

		return tableQueries.select(records, offset, amount);
	}

	/*
	 * Allow both PUT and POST methods for ease of use.
	 */
	@RequestMapping(value = "/insert", method = { RequestMethod.PUT, RequestMethod.POST })
	public List<Map<String, Object>> insert(@RequestBody List<Map<String, Object>> records) {
		return tableQueries.insert(records);
	}

	@PostMapping("/update")
	public int update(@RequestBody List<Map<String, Object>> records) {
		return tableQueries.update(records);
	}

	@PostMapping("/update/all")
	public int updateAll(@RequestBody List<Map<String, Object>> records) {
		return tableQueries.update(records, true);
	}

	@DeleteMapping("/delete/all")
	public int deleteAll() {
		return tableQueries.deleteAll();
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.DELETE, RequestMethod.POST })
	public int delete(@RequestBody List<Map<String, Object>> records) {
		return tableQueries.delete(records);
	}

}

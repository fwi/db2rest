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
    protected final boolean queryColumnTypesAtStartup;

	public RestTable(RestTableQueries tableQueries) {
		this(tableQueries, tableQueries.timestampColumns.isEmpty());
	}

	public RestTable(RestTableQueries tableQueries, boolean queryColumnTypesAtStartup) {
		this.tableQueries = tableQueries;
		this.queryColumnTypesAtStartup = queryColumnTypesAtStartup;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!queryColumnTypesAtStartup) {
			return;
		}
		var timestampColumns = tableQueries.timestampColumns;
		if (timestampColumns.isEmpty()) {
			@SuppressWarnings("unchecked")
			var meta = (List<Map<String, Object>>) meta();
			@SuppressWarnings("unchecked")
			var columns = (List<Map<String, Object>>) meta.get(0).get("columns");
			for (var column : columns) {
				if (((String) column.get("TYPE_NAME")).equalsIgnoreCase("TIMESTAMP")) {
					timestampColumns.add((String) column.get("COLUMN_NAME"));
				}
			}
			if (timestampColumns.size() > 0) {
				log.debug("Found {} timestamp columns for table {}. Columns: {}", timestampColumns.size(), tableQueries.tableName, timestampColumns);
			} else {
				log.debug("Found no timestamp columns for table {}.", tableQueries.tableName);
			}
		}
	}

    /*
     * Everything in this class that deals with tables
     * communicates via a List<Map<String, Object>>,
     * both as input and as output.
     */

    @GetMapping("/meta")
    public List<?> meta() {
    	return tableQueries.meta();
    }

    @GetMapping("/select/all")
    public List<?> selectAll() {
    	return tableQueries.selectAll();
    }

    /*
     * GET with a body is not always supported, allow POST as well.
     */
    @RequestMapping(value = "/select", method = {RequestMethod.GET, RequestMethod.POST})
    public List<?> select(
            @RequestBody(required = false) List<Map<String, Object>> records,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "0") int amount
            ) {

        return tableQueries.select(records, offset, amount);
    }

    /*
     * Allow both PUT and POST methods for ease of use.
     */
    @RequestMapping(value = "/insert", method = {RequestMethod.PUT, RequestMethod.POST})
    public List<?> insert(@RequestBody List<Map<String, Object>> records) {
    	return tableQueries.insert(records);
    }
    
    @PostMapping("/update")
    public List<?> update(@RequestBody List<Map<String, Object>> records) {
    	return tableQueries.update(records);
    }

    @PostMapping("/update/all")
    public List<?> updateAll(@RequestBody List<Map<String, Object>> records) {
    	return tableQueries.update(records, true);
    }

    @DeleteMapping("/delete/all")
    public List<?> deleteAll() {
    	return tableQueries.deleteAll();
    }

    @RequestMapping(value = "/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
    public List<?> delete(@RequestBody List<Map<String, Object>> records) {
    	return tableQueries.delete(records);
    }

}

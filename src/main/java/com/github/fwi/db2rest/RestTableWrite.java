package com.github.fwi.db2rest;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class RestTableWrite extends RestTableRead {

	public RestTableWrite(RestTableQueries tableQueries) {
		this(tableQueries, true);
	}

	public RestTableWrite(RestTableQueries tableQueries, boolean queryColumnsAtStartup) {
		super(tableQueries, queryColumnsAtStartup);
	}

	/*
	 * Allow both PUT and POST methods for ease of use.
	 */
	@RequestMapping(value = "/insert/one", method = { RequestMethod.PUT, RequestMethod.POST })
	public Map<String, Object> insertOne(@RequestBody Map<String, Object> record) {
		return tableQueries.insert(record);
	}

	@RequestMapping(value = "/insert", method = { RequestMethod.PUT, RequestMethod.POST })
	public Map<String, Object> insert(@RequestBody List<Map<String, Object>> records) {
		return tableQueries.insert(records);
	}

	@PostMapping("/update/one")
	public int updateOne(@RequestBody Map<String, Object> record) {
		return tableQueries.update(record);
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

	@RequestMapping(value = "/delete/one", method = { RequestMethod.DELETE, RequestMethod.POST })
	public int delete(@RequestBody Map<String, Object> record) {
		return tableQueries.delete(record);
	}

	@RequestMapping(value = "/delete", method = { RequestMethod.DELETE, RequestMethod.POST })
	public int delete(@RequestBody List<Map<String, Object>> records) {
		return tableQueries.delete(records);
	}

}

package com.github.fwi.db2rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public class RestTable extends RestTableInitializer {

	public RestTable(RestTableQueries tableQueries) {
		this(tableQueries, true);
	}

	public RestTable(RestTableQueries tableQueries, boolean queryColumnsAtStartup) {
		super(tableQueries, queryColumnsAtStartup);
	}
	@GetMapping("/meta")
	public Map<String, Object> meta() {
		return tableQueries.meta();
	}

	@GetMapping("/select/all")
	public List<Map<String, Object>> selectAll() {
		return tableQueries.selectAll();
	}

	@GetMapping("/select/{column}/{value}")
	public List<Map<String, Object>> selectOne(
			@PathVariable String column, 
			@PathVariable String value,
			@RequestParam(value = "type", defaultValue = StringUtils.EMPTY) String type) {
		
		Object typedValue = value;
		if (type != null && type.length() > 0) {
			switch (type) {
				case "string": 
					break;
				case "number":
					typedValue = Long.valueOf(value);
					break;
				case "switch":
					typedValue = Boolean.valueOf(value);
					break;
				default:
					throw new BadRequestException("Unknown type " + type + "(must be one of string, number or switch");
			}
		} else {
			if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
				typedValue = Boolean.valueOf(value);
			} else {
				try {
					typedValue = Long.valueOf(value);
				} catch (Exception ignored) {
					//
				}
			}
		}
		return tableQueries.select(Collections.singletonList(Collections.singletonMap(column, typedValue)), 0, 0);
	}

	/*
	 * GET with a body is not always supported, allow POST as well.
	 */
	@RequestMapping(value = "/select", method = { RequestMethod.GET, RequestMethod.POST })
	public List<Map<String, Object>> select(
			@RequestBody(required = false) List<Map<String, Object>> records,
			@RequestParam(defaultValue = "0") int offset, 
			@RequestParam(defaultValue = "0") int amount) {

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

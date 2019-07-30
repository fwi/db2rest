package com.github.fwi.db2rest;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public class RestTableRead extends RestTableInitializer {

	public RestTableRead(RestTableQueries tableQueries) {
		this(tableQueries, true);
	}

	public RestTableRead(RestTableQueries tableQueries, boolean queryColumnsAtStartup) {
		super(tableQueries, queryColumnsAtStartup);
	}

	@GetMapping("/meta")
	public Map<String, Object> meta() {
		return tableQueries.meta();
	}

	@GetMapping("/select/all")
	public Map<String, Object> selectAll() {
		return tableQueries.selectAll();
	}

	@GetMapping("/select/{column}/{value}")
	public Map<String, Object> selectFromOneColumn(
		@PathVariable String column,
		@PathVariable String value,
		@RequestParam(value = "type", defaultValue = StringUtils.EMPTY) String type) {

		Object typedValue = value;
		if (type != null && type.length() > 0) {
			switch (type) {
			case "text":
				break;
			case "number":
				try {
					typedValue = Long.valueOf(value);
				} catch (Exception e) {
					throw new BadRequestException("Value is not a number: " + value);
				}
				break;
			case "switch":
				typedValue = Boolean.valueOf(value);
				break;
			default:
				throw new BadRequestException("Unknown type " + type + "(must be one of text, number or switch");
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
		return tableQueries.select(column, typedValue);
	}

	/*
	 * GET with a body is not always supported, allow POST as well.
	 */
	@RequestMapping(value = "/select", method = { RequestMethod.GET, RequestMethod.POST })
	public Map<String, Object> select(
		@RequestBody(required = false) List<Map<String, Object>> records,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "0") int amount) {

		return tableQueries.select(records, offset, amount);
	}

}

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

public class RestTableRead extends TableInitializer {

	public RestTableRead(TableQueries tableQueries) {
		this(tableQueries, true);
	}

	public RestTableRead(TableQueries tableQueries, boolean queryColumnsAtStartup) {
		super(tableQueries, queryColumnsAtStartup);
	}

	public String tableName() {
		return tableQueries.meta.tableName();
	}

	@GetMapping("/meta")
	public Map<String, Object> meta() {
		return tableQueries.metaData();
	}

	/*
	 * GET with a body is not always supported, allow POST as well.
	 */
	@RequestMapping(value = "/select", method = { RequestMethod.GET, RequestMethod.POST })
	public List<Map<String, Object>> select(
		@RequestBody(required = false) List<Map<String, Object>> records,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "0") int limit) {

		return tableQueries.select(records, offset, limit);
	}

	@GetMapping("/select/{column}/{value}")
	public List<Map<String, Object>> selectFromOneColumn(
		@PathVariable String column,
		@PathVariable String value,
		@RequestParam(value = "valuetype", defaultValue = StringUtils.EMPTY) String valuetype,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "0") int limit) {

		return tableQueries.select(column, typedValue(valuetype, value), offset, limit);
	}

	@GetMapping("/select/{column}/{op}/{value}")
	public List<Map<String, Object>> selectFromOneColumn(
		@PathVariable String column,
		@PathVariable String op,
		@PathVariable String value,
		@RequestParam(value = "valuetype", defaultValue = StringUtils.EMPTY) String valuetype,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "0") int limit) {

		return tableQueries.select(column, op, typedValue(valuetype, value), offset, limit);
	}
	
	@GetMapping("/select/one/{column}/{value}")
	public Map<String, Object> selectOne(
		@PathVariable String column,
		@PathVariable String value,
		@RequestParam(value = "valuetype", defaultValue = StringUtils.EMPTY) String valuetype) {

		var records = selectFromOneColumn(column, value, valuetype, 0, 2);
		if (records.size() == 1) {
			return records.get(0);
		} else if (records.size() == 0) {
			throw new NotFoundException("No record found in " + tableName() + " for " + column + " " + value);
		} else {
			throw new BadRequestException(
				"Found more than one record in " + tableName() + " for " + column + " " + value);
		}
	}

	@GetMapping("/select/all")
	public List<Map<String, Object>> selectAll() {
		return tableQueries.selectAll();
	}
	
	/**
	 * Calls {@link #toTypedValue(String, String)} to convert value to a value of correct class.
	 */
	public Object typedValue(String type, String value) {
		return toTypedValue(type, value);
	}

	/**
	 * Default implementation for {@link #typedValue(String, String)} which tries
	 * to set a correct class-type for the value (Boolean, Long or String).
	 * Boolean (or "switch") conversion follows the rules of {@link Boolean#valueOf(String)},
	 * number conversion follows the rules of {@link Long#valueOf(String)}.
	 * @param type if empty, value class-type is guessed else "text", "number" or "switch".
	 * @param value the value to examine.
	 * @return The value converted to correct class-type.
	 */
	public static Object toTypedValue(String type, String value) {

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
		return typedValue;
	}

}

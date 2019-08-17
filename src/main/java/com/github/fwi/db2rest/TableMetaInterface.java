package com.github.fwi.db2rest;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface TableMetaInterface {

	String schema();

	String tableName();

	Collection<String> columnNames();

	Collection<String> selectOnlyColumns();

	Collection<String> timestampColumns();

	Map<String, Object> insertDefaults();

	/**
	 * Default amount (limit) of number of records to return.
	 * Default 1 000.
	 */
	int maxAmountDefault();

	/**
	 * Absolute amount (limit) of number of records to return.
	 * Default 10 000, use 0 to indicate no limit.
	 * @return
	 */
	int maxAmountAbsolute();

	ObjectMapper mapper();

	OffsetDateTime toTimestamp(String value);

}

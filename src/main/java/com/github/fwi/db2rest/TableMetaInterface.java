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

	int maxAmountDefault();

	ObjectMapper mapper();

	OffsetDateTime toTimestamp(String value);
}

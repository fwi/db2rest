package com.github.fwi.db2restapp;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.fwi.db2rest.RestTable;
import com.github.fwi.db2rest.RestTableQueries;

@RestController
@RequestMapping("/task")
public class TableTask extends RestTable {

	public TableTask(RestTableQueries tableQueries) {
		super(tableQueries);
	}

	@GetMapping(value = { "", "/" })
	List<?> home() {
		return tableQueries.asList(tableQueries.tableName);
	}

	@GetMapping("/time")
	List<?> time() {
		return tableQueries.asList(OffsetDateTime.now());
	}

	@GetMapping("/time/utc")
	List<?> timeUtc() {
		return tableQueries.asList(OffsetDateTime.now(ZoneOffset.UTC));
	}

}

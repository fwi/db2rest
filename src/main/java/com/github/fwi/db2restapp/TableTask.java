package com.github.fwi.db2restapp;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.fwi.db2rest.RestTableQueries;
import com.github.fwi.db2rest.RestTableWrite;

@RestController
@RequestMapping("/task")
public class TableTask extends RestTableWrite {

	public TableTask(RestTableQueries tableQueries) {
		super(tableQueries);
	}

	@GetMapping(value = { "", "/" })
	String home() {
		return tableQueries.tableName;
	}

	@GetMapping("/time")
	OffsetDateTime time() {
		return OffsetDateTime.now();
	}

	@GetMapping("/time/utc")
	OffsetDateTime timeUtc() {
		return OffsetDateTime.now(ZoneOffset.UTC);
	}

}

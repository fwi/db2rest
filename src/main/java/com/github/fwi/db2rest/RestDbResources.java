package com.github.fwi.db2rest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class RestDbResources {

    public final JdbcTemplate jdbcTemplate;

    public final NamedParameterJdbcTemplate namedJdbcTemplate;

    public final TransactionTemplate transactionTemplate;

	public RestDbResources(
			JdbcTemplate jdbcTemplate, 
			NamedParameterJdbcTemplate namedJdbcTemplate,
			TransactionTemplate transactionTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedJdbcTemplate = namedJdbcTemplate;
		this.transactionTemplate = transactionTemplate;
	}

}

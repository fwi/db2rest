package com.github.fwi.db2rest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public interface DbTemplatesInterface {

	JdbcTemplate jdbcTemplate();

	NamedParameterJdbcTemplate namedJdbcTemplate();

	TransactionTemplate transactionTemplate();
}

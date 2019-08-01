package com.github.fwi.db2rest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class DbTemplates implements DbTemplatesInterface {

	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedJdbcTemplate;
	private TransactionTemplate transactionTemplate;

	public DbTemplates(
		JdbcTemplate jdbcTemplate,
		NamedParameterJdbcTemplate namedJdbcTemplate,
		TransactionTemplate transactionTemplate) {

		this.jdbcTemplate = jdbcTemplate;
		this.namedJdbcTemplate = namedJdbcTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public JdbcTemplate jdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public NamedParameterJdbcTemplate namedJdbcTemplate() {
		return namedJdbcTemplate;
	}

	@Override
	public TransactionTemplate transactionTemplate() {
		return transactionTemplate;
	}

}

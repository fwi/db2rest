package com.github.fwi.db2restapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fwi.db2rest.RestDbResources;
import com.github.fwi.db2rest.RestTableMeta;
import com.github.fwi.db2rest.RestTableQueries;

@Configuration
@EnableAutoConfiguration
public class App {

	public static void main(String[] args) {
		new SpringApplication(App.class).run(args);
	}

	@Bean
	public RestDbResources restDbResources(
			JdbcTemplate jdbcTemplate, 
			NamedParameterJdbcTemplate namedJdbcTemplate,
			TransactionTemplate transactionTemplate) {
		return new RestDbResources(jdbcTemplate, namedJdbcTemplate, transactionTemplate);
	}

	@Bean
	public TableTask taskRestApi(RestDbResources restDbResources, ObjectMapper objectMapper) {
		
		var tableMeta = RestTableMeta.builder("task")
				// these columns can never be updated but can be used for selection of a record to update
				.addSelectOnlyColumns("id", "created", "modified")
				// an insert without the "completed" column gets this default value 
				.addInsertDefault("completed", false)
				// timestamp-columns are auto-discovered in RestTable.afterPropertiesSet()
				// .addTimestampColumns("created", "modified")
				.build();
		return new TableTask(
				new RestTableQueries(tableMeta, restDbResources, objectMapper));
	}

}

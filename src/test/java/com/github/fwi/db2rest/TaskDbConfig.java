package com.github.fwi.db2rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.zaxxer.hikari.HikariDataSource;

/*
 * Example template for configuring several datasources (databases).
 * To configure a second datasource, copy this template, change the method names,
 * change the db-templates bean name (use it as @Qualifier),
 * and add configuration-properties under a new name (e.g. "db2rest.datasource.seconddb").
 *  
 * See TeskTask.TestTaskConfig for example usage.
 * 
 * This configuration setup requires disabling the datasource-autoconfiguration.
 * If datasource-autoconfiguration is required, mark at least one datasource as @Primary.
 */
@Configuration
@ConditionalOnProperty(name = "db2rest.taskdb.enabled")
public class TaskDbConfig {
	
	@Bean("task-dbtemplates")
	public DbTemplates taskDbTemplates() {
		return new DbTemplates(taskJdbcTemplate(), taskNamedParameterJdbcTemplate(), taskTxTemplate());
	}
	
	@Bean
	@ConfigurationProperties("db2rest.datasource.task")
	public DataSourceProperties taskDataSourceProperties() {
		return new DataSourceProperties();
	}
	
	@Bean
	@ConfigurationProperties("db2rest.datasource.task.hikari")
	public HikariDataSource taskDataSource() {
		return taskDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean
	public DataSourceInitializer taskDsInit(ResourceLoader resourceLoader) {
		
		// load scripts to run into a database-populator.
		
		var schemas = taskDataSourceProperties().getSchema();
		var datas = taskDataSourceProperties().getData();
		if ((schemas == null || schemas.isEmpty())
			&& (datas == null || datas.isEmpty())) {
			return null; // no scripts to run at startup.
		}
		var dsPop = new ResourceDatabasePopulator();
		if (schemas != null) {
			schemas.stream().forEach(e -> dsPop.addScript(resourceLoader.getResource(e)));
		}
		if (datas != null) {
			datas.stream().forEach(e -> dsPop.addScript(resourceLoader.getResource(e)));
		}
		var dsInit = new DataSourceInitializer();
		dsInit.setDatabasePopulator(dsPop);
		dsInit.setDataSource(taskDataSource());
		return dsInit;
	}
	
	@Bean
	public JdbcTemplate taskJdbcTemplate() {
		return new JdbcTemplate(taskDataSource());
	}
	
	@Bean
	public NamedParameterJdbcTemplate taskNamedParameterJdbcTemplate() {
		return new NamedParameterJdbcTemplate(taskDataSource());
	}
	
	@Bean
	public PlatformTransactionManager taskTxManager() {
		// This bean is required, unless DataSourceTransactionManagerAutoConfiguration
		// is excluded from autoconfiguration as well.
		return new DataSourceTransactionManager(taskDataSource());
	}
	
	@Bean
	public TransactionTemplate taskTxTemplate() {
		return new TransactionTemplate(taskTxManager());
	}

}

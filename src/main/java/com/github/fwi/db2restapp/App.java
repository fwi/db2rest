package com.github.fwi.db2restapp;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fwi.db2rest.DbTemplates;
import com.github.fwi.db2rest.TableMeta;
import com.github.fwi.db2rest.TableQueries;
import com.github.fwi.lorm2rest.TaskRepo;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableAutoConfiguration
@EnableSwagger2
public class App {

	public static void main(String[] args) {
		new SpringApplication(App.class).run(args);
	}

	@Autowired
	ObjectMapper mapper;

	@Bean
	public DbTemplates restDbTemplates(
		JdbcTemplate jdbcTemplate,
		NamedParameterJdbcTemplate namedJdbcTemplate,
		TransactionTemplate transactionTemplate) {

		return new DbTemplates(jdbcTemplate, namedJdbcTemplate, transactionTemplate);
	}

	@Bean
	public TableTask taskRestApi(DbTemplates dbTemplates) {

		var tableMeta = TableMeta.builder("task", mapper)
			// these columns can never be updated but can be used for selection of a record to update
			.selectOnlyColumns("id", "created", "modified")
			// an insert without the "completed" column gets this default value 
			.insertDefault("completed", false)
			// timestamp-columns are auto-discovered in TableInitializer.afterPropertiesSet()
			// .timestampColumns("created", "modified")
			.build();
		return new TableTask(
			new TableQueries(tableMeta, dbTemplates));
	}

	@Bean
	public TaskRepo taskRepoRestApi(DbTemplates dbTemplates) {
		return new TaskRepo(dbTemplates);
	}

	@Bean
	public AppTableMappings appTableMappings() {
		return new AppTableMappings();
	}

	@Bean
	@ConditionalOnProperty(name = "db2rest.security.enabled", matchIfMissing = true)
	public AppBasicAuth appBasicAuth() {
		return new AppBasicAuth();
	}

	@Bean
	@ConditionalOnBean(AppBasicAuth.class)
	public HttpFirewall httpFirewall() {
		
		/*
		 * Values often contains special characters, these need to be allowed.
		 */
		var firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedPercent(true);
		firewall.setAllowUrlEncodedPeriod(true);
		firewall.setAllowUrlEncodedSlash(true);
		return firewall;
	}
	
	@Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> {
            SwaggerResource wsResource = new SwaggerResource();
            wsResource.setName("db2rest spec");
            wsResource.setSwaggerVersion("2.0");
            wsResource.setLocation("/swagger.yaml");

            List<SwaggerResource> resources = new ArrayList<>();
            resources.add(wsResource);
            return resources;
        };
    }
	
	@Bean
	public Docket apiDocs(ServletContext servletContext) {
		return new Docket(DocumentationType.SWAGGER_2)
			.host("localhost")
			.pathProvider(new RelativePathProvider(servletContext) {
                @Override
                public String getApplicationBasePath() {
                    return "/";
                }
            }).enable(false);
	}

}

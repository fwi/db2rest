package com.github.fwi.db2rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fwi.db2rest.DbTemplates;
import com.github.fwi.db2rest.TableMeta;
import com.github.fwi.db2rest.TableQueries;
import com.github.fwi.db2restapp.AppTableMappings;
import com.github.fwi.db2restapp.TableTask;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestTask.TestTaskConfig.class)
@ActiveProfiles("test-task")
public class TestTask {

	final Logger log = LoggerFactory.getLogger(TestTask.class);

	@Autowired
	TestRestTemplate restTemplate;

	@Autowired
	ObjectMapper mapper;

	@Test
	public void testSelect() throws Exception {

		var t = restTemplate.getForObject("/db2rest/text", String.class);
		log.debug("db2rest text:\n{}", t);
		assertTrue(t
			.contains("/task/select/{column}/{value} [GET] (valuetype default=[], offset default=[0], limit default=[0])"));

		var s = restTemplate.getForObject("/task", String.class);
		log.debug("Response: {}", s);
		assertEquals(s, restTemplate.getForObject("/task/", String.class));

		var o = restTemplate.getForObject("/task/meta", String.class);
		log.trace("Response: {}", o);
		Map<String, Object> meta = castMap(mapper.readValue(o, Map.class));
		var columns = castListString(meta.get("columnnames"));
		log.debug("Columns: " + columns);
		assertTrue(columns.contains("created") && columns.contains("description"));

		var one = restTemplate.getForObject("/task/select/id/1", String.class);
		log.debug("Response: {}", one);
		var records = castListMap(mapper.readValue(one, List.class));
		var desc = records.get(0).get("description");
		assertTrue(desc.equals("mop the floor"));

		var completed = restTemplate.getForObject("/task/select/completed/false?valuetype=switch", String.class);
		log.debug("Response: {}", completed);
		records = castListMap(mapper.readValue(completed, List.class));
		var id = records.get(0).get("id");
		assertTrue((int) id == 2);

		var filtered = restTemplate.getForObject("/task/select/description/like/%e ov%", String.class);
		log.debug("Response: {}", filtered);
		records = castListMap(mapper.readValue(filtered, List.class));
		assertTrue(records.size() > 0);
		for (var record : records) {
			desc = record.get("description");
			assertTrue(desc.toString().contains("e ov"));
		}
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> castMap(Object o) {
		return (Map<String, Object>) o;
	}

	@SuppressWarnings("unchecked")
	List<String> castListString(Object o) {
		return (List<String>) o;
	}

	@SuppressWarnings("unchecked")
	List<Map<String, Object>> castListMap(Object o) {
		return (List<Map<String, Object>>) o;
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration(exclude = { 
		DataSourceAutoConfiguration.class, 
		DataSourceTransactionManagerAutoConfiguration.class,
		SecurityAutoConfiguration.class
	})
	@Import({ TaskDbConfig.class })
	static class TestTaskConfig {

		@Autowired
		ObjectMapper mapper;

		@Bean
		@ConditionalOnBean(name = "task-dbtemplates")
		public TableTask tableTask(@Qualifier("task-dbtemplates") DbTemplates dbTemplates) {

			var tableMeta = TableMeta.builder("task", mapper)
				.selectOnlyColumns("id", "created", "modified")
				.insertDefault("completed", false)
				.build();

			return new TableTask(
				new TableQueries(tableMeta, dbTemplates));
		}

		@Bean
		public AppTableMappings appTableMappings() {
			return new AppTableMappings();
		}

	}

}

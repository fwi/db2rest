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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fwi.db2rest.RestDbResources;
import com.github.fwi.db2rest.RestTableMeta;
import com.github.fwi.db2rest.RestTableQueries;
import com.github.fwi.db2restapp.AppTableMappings;
import com.github.fwi.db2restapp.TableTask;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestTask.TestTaskConfig.class)
@ActiveProfiles("test-task")
public class TestTask {

	final Logger log = LoggerFactory.getLogger(TestTask.class);

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void testSelect() throws Exception {
		
		var t = restTemplate.getForObject("/db2rest", String.class);
		var tdata = getData(mapper.readValue(t, Map.class));
		// most interesting to test is /task/select/{column}/{value}
		// but to lazy to do it here.
		assertTrue(tdata.size() > 0);
		
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
		var records = getData(mapper.readValue(one, Map.class));
		var desc = records.get(0).get("description");
		assertTrue(desc.equals("mop the floor"));

		var completed = restTemplate.getForObject("/task/select/completed/false?type=switch", String.class);
		log.debug("Response: {}", completed);
		records = getData(mapper.readValue(completed, Map.class));
		var id = records.get(0).get("id");
		assertTrue( (int)id == 2);
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
	
	@SuppressWarnings("unchecked")
	List<Map<String, Object>> getData(Object o) {
		return (List<Map<String, Object>>)((Map<String, Object>) o).get(RestTableQueries.DATA_KEY);
	}
	
	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestTaskConfig {
		
		@Bean
		public RestDbResources restDbResources(
				JdbcTemplate jdbcTemplate, 
				NamedParameterJdbcTemplate namedJdbcTemplate,
				TransactionTemplate transactionTemplate) {
			return new RestDbResources(jdbcTemplate, namedJdbcTemplate, transactionTemplate);
		}

		@Bean
		public TableTask tableTask(RestDbResources restDbResources, ObjectMapper objectMapper) {
			
			var tableMeta = RestTableMeta.builder("task")
					.selectOnlyColumns("id", "created", "modified")
					.insertDefault("completed", false)
					.build();
			return new TableTask(
					new RestTableQueries(tableMeta, restDbResources, objectMapper));
		}
		
		@Bean
		public AppTableMappings appTableMappings() {
			return new AppTableMappings();
		}

	}

}

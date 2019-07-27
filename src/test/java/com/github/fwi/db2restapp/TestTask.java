package com.github.fwi.db2restapp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestTask {
	
	final Logger log = LoggerFactory.getLogger(TestTask.class);

	@Autowired
	private TestRestTemplate restTemplate;
	
	@Test
	public void testSelect() {
		var s = restTemplate.getForObject("/task", String.class);
		log.info("Response: {}", s);
		assertEquals(s, restTemplate.getForObject("/task/", String.class));
	}

}

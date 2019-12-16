package com.github.fwi.lorm2rest;

import java.util.List;

import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.fwi.db2rest.DbTemplatesInterface;
import com.github.fwi.db2rest.NotFoundException;

/**
 * Attempt a "lightweight ORM" (lorm) implementation.
 * 
 * When started from the "test-local" configuration (see this projects' README),
 * various endpoints become available under <tt>/repo</tt>, e.g. <tt>http://localhost:8082/repo/task/groups</tt>
 * <p>
 * This repository implementation only serves as a demonstration 
 * for an alternative JDBC-implementation where relations between tables can be expressed using POJOs.
 * <p>
 * The relations in this implementation:
 * <pre>
task (n) <--> (1) person (n) <--> (n) group
 * </pre>
 * A task is related to one person. A person can be in multiple groups and a group can have multiple persons.
 * <br>The <tt>simpleflatmapper</tt> factory extracts all these relations from query result-sets
 * and creates and fills the various POJOs.
 * <p>
 * The <tt>simpleflatmapper</tt> is not designed to handle many-to-many relations
 * but this implementation does it anyway (via table <tt>person_task_group</tt>). 
 * This in turn shows where these functions hit the limits.
 * E.g. the query for <tt>http://localhost:8082/repo/task/groups</tt> provides proper results,
 * but the underlying query has the potential to generate very large result-sets for not much data
 * (i.e. can be very inefficient).
 *
 */
@RestController
@RequestMapping("/repo/task")
public class TaskRepo {
	
	private final DbTemplatesInterface dbTemplates;
	
	public TaskRepo(DbTemplatesInterface dbTemplates) {
		this.dbTemplates = dbTemplates;
	}
	
	private final ResultSetExtractor<List<Task>> taskMapper = JdbcTemplateMapperFactory
		.newInstance()
		.addKeys("id", "executor_id", "executor_group_id")
		.newResultSetExtractor(Task.class);

	public static final String TASK_SELECT_ORDER = "order by t.id, p.id, g.id";

	public static final String TASK_SELECT = 
		"select t.id as id, t.description, t.completed, t.created, t.modified, " +
		"p.id as executor_id, p.name as executor_name, " +
		"g.id as executor_group_id, g.name as executor_group_name " +
		"from task t " +
		"left join person p on p.id = t.executor " +
		"left join person_task_group pg on pg.person = p.id " +
		"left join task_group g on g.id = pg.task_group";

	@GetMapping("/all")
	public List<Task> findTasks() {
		return dbTemplates.jdbcTemplate().query(TASK_SELECT + " " + TASK_SELECT_ORDER, taskMapper);
	}

	@GetMapping("/id/{value}")
	public Task findTaskById(@PathVariable Long value) {
		
		var tasks = dbTemplates.jdbcTemplate().query(
			TASK_SELECT + " where t.id = ? " + TASK_SELECT_ORDER, taskMapper, value);
		if (tasks.isEmpty()) {
			throw new NotFoundException("No task with ID " + value + " found.");
		}
		return tasks.get(0);
	}

	private final ResultSetExtractor<List<Person>> personMapper = JdbcTemplateMapperFactory
		.newInstance()
		.addKeys("id", "group_id", "task_id")
		.newResultSetExtractor(Person.class);

	public static final String PERSON_SELECT_ORDER = "order by p.id, g.id, t.id";

	public static final String PERSON_SELECT =
		"select p.id as id, p.name, " +
		"g.id as group_id, g.name as group_name, " +
		"t.id as task_id, t.description as task_description, t.completed as task_completed, t.created as task_created, t.modified as task_modified " +
		"from person p " +
		"left join person_task_group pg on pg.person = p.id " +
		"left join task_group g on g.id = pg.task_group " +
		"left join task t on t.executor = p.id";

	@GetMapping("/persons")
	public List<Person> findPersons() {
		return dbTemplates.jdbcTemplate().query(PERSON_SELECT + " " + PERSON_SELECT_ORDER, personMapper);
	}

	private final ResultSetExtractor<List<TaskGroup>> groupMapper = JdbcTemplateMapperFactory
		.newInstance()
		.addKeys("id", "person_id", "task_id", "task_executor_id")
		.newResultSetExtractor(TaskGroup.class);

	public static final String GROUP_SELECT_ORDER = "order by g.id, p.id, t.id, tp.id";

	/*
	 * Persons is joined twice in this query for different purposes.
	 * The first join is to fill the group.persons-list.
	 * The last join is to fill the task.executor information.
	 * These two cannot be combined - they are two different "sets" of data.
	 */
	public static final String GROUP_SELECT =
		"select g.id as id, g.name, " +
		"p.id as person_id, p.name as person_name, " +
		"t.id as task_id, t.description as task_description, t.completed as task_completed, t.created as task_created, t.modified as task_modified, " +
		"tp.id as task_executor_id, tp.name as task_executor_name " +
		"from task_group g " +
		"left join person_task_group pg on pg.task_group = g.id " +
		"left join person p on p.id = pg.person " +
		"left join task t on t.executor = p.id " +
		"left join person tp on tp.id = t.executor";

	@GetMapping("/groups")
	public List<TaskGroup> findTaskGroups() {
		return dbTemplates.jdbcTemplate().query(GROUP_SELECT + " " + GROUP_SELECT_ORDER, groupMapper);
	}

}

Db2Rest
-------

A flexible REST interface enabling direct (SQL) database operations. Uses Spring Boot. 

Inspired by [ripping out Hibernate](https://dev.to/jillesvangurp/ripping-out-hibernate-and-going-native-jdbc-1lf2)
and the power of "database definition to CRUD display" as demonstrated by [flask-admin](https://github.com/flask-admin/flask-admin)
(which does use Hibernate).

This is a demo-project, not a library that can be re-used.
Basic functionality is provided in this project and can be used as a starting point 
for creating a simple, yet powerfull REST interface for your own application.

The main benefit of sticking with plain JDBC instead of using Hibernate
is that you can do weird, optimized and unique database operations 
without jumping through hoops to get what you need.   

Run this project from the project's root with the command:

	# Enable Java 11 in your environment, e.g. "set JAVA_HOME=c:\java\jdk11"
	mvn -B -q spring-boot:run -Dspring.config.location=file:./test-local/

In your browser go to

- http://localhost:8082/task/time
- http://localhost:8082/task/meta
- http://localhost:8082/task/select/all

For reference the demo-table "task":

```SQL
CREATE TABLE task (
  id          identity,
  created     timestamp not null default CURRENT_TIMESTAMP,
  modified    timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description VARCHAR(64) NOT NULL,
  completed   BIT NOT NULL);
```

The entire Spring Boot wiring for the "task" table comes down to the following configuration 
(see [App.java](./src/main/java/com/github/fwi/db2restapp/App.java)):

```java
	@Bean
	public TableTask taskRestApi(RestDbResources restDbResources, ObjectMapper objectMapper) {
		
		var tableMeta = RestTableMeta.builder("task")
				// these columns can never be updated but can be used for selection of a record to update
				.selectOnlyColumns("id", "created", "modified")
				// an insert without the "completed" column gets this default value 
				.insertDefault("completed", false)
				// timestamp-columns are auto-discovered in RestTable.afterPropertiesSet()
				// .timestampColumns("created", "modified")
				.build();
		return new TableTask(
				new RestTableQueries(tableMeta, restDbResources, objectMapper));
	}
```

With the project running, open a second terminal in this project's `test-local` directory
and run the `curl` commands shown in [curl-cmds.txt](./test-local/curl-cmds.txt)

The contents of the JSON-files in the `/test-local` directory
should give an idea of the functions provided by the rest-interface:

A simple select (returns records with id 3 and 4):

```JSON
[{
	"id": [3, 4]
}]
```

A complicated select (returns records that are not completed, created after a certain date and with id 2, 4 or 6):

```JSON
[{
	"rest2db_query_filters": [
		{"column": "completed", "op": "=", "value": false},
		{"column": "created", "op": ">=", "value": "2019-07-27T16:36:48.035+0000"},
		{"column": "id", "op": "in", "value": [2,4,6]}
	]
}]
```

A simple update (set completed to false for record 3, update the description for record 4):

```JSON
[{
	"id": 3,
	"completed": false
},
{
	"id": 4,
	"description": "wash the laundry"
}]
```

A complicated update (set completed to true for records created after a certain date and that contain "laundry" in the description):

```JSON
 [{
	"completed": true,
	"rest2db_query_filters": [
		{"column": "created", "op": ">=", "value": "2019-07-27T16:36:48.035+0000"},
		{"column": "description", "op": "ilike", "value": "%laundry%"}
	]
}]
```

If you like these functions, copy the classes involved (clone this repo)
and see if you can implement the `DB_QUERY_ORDER` function
(TODO in [RestTableQueries.java](./src/main/java/com/github/fwi/db2rest/RestTableQueries.java))
so that results can be sorted. If that takes a long time to implement,
consider rolling your own (copy the good parts).
If it did not take you a long time and you did not get lost in the source code,
this might be a decent start for your own REST interface using plain JDBC.

A final note on table- and  column-names (one of the hurdles along the way):
Postgresql converts unquoted names to lowercase,
everybody else converts unquoted names to UPPERCASE.
To prevent maintenance hell, always use lower\_case names with under\_scores.
For H2 database, mimic Postgresql with jdbc-url parameter "database\_to\_upper=false"
so that unquoted names can be used in queries and DDL statements. 


`EOF`
package com.github.fwi.lorm2rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Person {

	private long id;
	private String name;
	private List<Task> tasks;
	private List<TaskGroup> groups;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public List<TaskGroup> getGroups() {
		return groups;
	}
	public void setGroups(List<TaskGroup> groups) {
		this.groups = groups;
	}

}

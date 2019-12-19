package com.github.fwi.lorm2rest;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Task {

	private long id;
	private String description;
	private boolean completed;
	private OffsetDateTime created;
	private OffsetDateTime modified;
	private Person executor;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	public OffsetDateTime getCreated() {
		return created;
	}
	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}
	public OffsetDateTime getModified() {
		return modified;
	}
	public void setModified(OffsetDateTime modified) {
		this.modified = modified;
	}
	public Person getExecutor() {
		return executor;
	}
	public void setExecutor(Person executor) {
		this.executor = executor;
	}
	
}

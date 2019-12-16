CREATE TABLE person (
  id          identity,
  name        VARCHAR(64) NOT NULL
);

CREATE TABLE task_group (
  id          identity,
  name        VARCHAR(64) NOT NULL
);

CREATE TABLE person_task_group (
  id          identity,
  person      BIGINT,
  task_group  BIGINT,
  UNIQUE(person, task_group),
  FOREIGN KEY (person) REFERENCES person(id) ON DELETE CASCADE,
  FOREIGN KEY (task_group) REFERENCES task_group(id) ON DELETE CASCADE
);

CREATE TABLE task (
  id          identity,
  created     timestamp not null default CURRENT_TIMESTAMP,
  modified    timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description VARCHAR(64) NOT NULL,
  executor    BIGINT,
  completed   BIT NOT NULL,
  FOREIGN KEY (executor) REFERENCES person(id)
);

CREATE TABLE task (
  id          identity,
  created     timestamp not null default CURRENT_TIMESTAMP,
  modified    timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description VARCHAR(64) NOT NULL,
  completed   BIT NOT NULL);

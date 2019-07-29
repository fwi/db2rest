-- Note on table- and column-names:
-- Postgresql converts unquoted names to lowercase,
-- everybody else converts unquoted names to UPPERCASE.
-- To prevent maintenance hell, always use lower_case names with under_scores.
-- For H2 database, mimic Postgresql with jdbc-url parameter "database_to_upper=false"
-- so that unquoted names can be used in queries and DDL statements. 

CREATE TABLE task (
  id          identity,
  created     timestamp not null default CURRENT_TIMESTAMP,
  modified    timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description VARCHAR(64) NOT NULL,
  completed   BIT NOT NULL);

INSERT INTO person (name) VALUES
  ('John'),
  ('Mary'),
  ('Fred');

INSERT INTO task_group (name) VALUES
  ('Alpha'),
  ('Beta'),
  ('Gamma');
  
INSERT INTO person_task_group (task_group, person) VALUES
  (1, 1),
  (1, 3),
  (3, 3);
--   (2, 2),
--   (2, 1),

INSERT INTO task (description, completed, executor) VALUES
  ('mop the floor', 1, 1),
  ('clean the oven', 0, 1),
  ('dust the living room', 1, NULL),
  ('hang the laundry', 0, 3);

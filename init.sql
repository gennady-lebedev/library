CREATE DATABASE library;

USE library;

CREATE TABLE publishers (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100),
  PRIMARY KEY (id)
);

CREATE TABLE authors (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50),
  PRIMARY KEY (id)
);

CREATE TABLE books (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100),
  isbn VARCHAR(20),
  publisher_id INT,
  PRIMARY KEY (id),
  FOREIGN KEY (publisher_id) REFERENCES publishers(id),
  UNIQUE (isbn)
);

CREATE TABLE books_authors (
  book_id INT NOT NULL,
  author_id INT NOT NULL,
  PRIMARY KEY (book_id, author_id),
  FOREIGN KEY (book_id) REFERENCES books(id),
  FOREIGN KEY (author_id) REFERENCES authors(id)
);

CREATE TABLE users (
  name VARCHAR(100) NOT NULL,
  password VARCHAR(60) NOT NULL,
  role VARCHAR(20),
  PRIMARY KEY (name)
);

CREATE TABLE items (
  id INT NOT NULL AUTO_INCREMENT,
  book_id INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  user VARCHAR(100) NOT NULL,
  place VARCHAR(50),
  due_date DATE,
  PRIMARY KEY (id),
  FOREIGN KEY (book_id) REFERENCES books(id),
  FOREIGN KEY (user) REFERENCES users(name)
);

CREATE TABLE item_logs (
  id INT NOT NULL AUTO_INCREMENT,
  item_id INT NOT NULL,
  item_status VARCHAR(20) NOT NULL,
  item_holder VARCHAR(100),
  item_due_date DATE,
  made_by VARCHAR(100) NOT NULL,
  timestamp TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (item_id) REFERENCES items(id),
  FOREIGN KEY (item_holder) REFERENCES users(name),
  FOREIGN KEY (made_by) REFERENCES users(name)
);

INSERT INTO publishers (id, title) VALUES
  (1, 'Manning Publications Co.'),
  (2, 'Packt Publishing Ltd.'),
  (3, 'O’Reilly Media, Inc.'),
  (4, 'The Pragmatic Programmers, LLC.'),
  (5, 'Prentice Hall');

INSERT INTO authors (id, name) VALUES
  (1, 'Craig Walls'),
  (2, 'Ron Jeffries'),
  (3, 'Cay S. Horstmann');

INSERT INTO books (id, title, isbn, publisher_id) VALUES
  (1, 'Core Java Volume I–Fundamentals, 10th Edition', '978-0-13-417730-4', 3),
  (2, 'The Nature of Software Development', '1-94122-237-4', 4),
  (3, 'Spring in Action, 4th Edition', '978-1-61729-120-3', 1),
  (4, 'Spring Boot in Action', '1-61729-254-0', 1);

INSERT INTO books_authors (book_id, author_id) VALUES
  (1, 3),
  (2, 2),
  (3, 1),
  (4, 1);

INSERT INTO users (name, password, role) VALUES
  ('Admin', '$2a$10$7BdS5724jFiHcMn77jYLte5cOHE9hGPvqiIH7bKJo9kOHSmyAixTO', 'ADMIN'),
  ('The Great Old Librarian', '$2a$10$.mkfXM5eDAYeMYfGOO5LGeGQYvk41Byty7hUaJ1r9dzjUTkAZTXum', 'LIBRARIAN'),
  ('Dummy Reader', '$2a$10$TiQe.YKwu8EX5roArIY3mewqSUihj6hYAs22Oj4W1SUY.LtiLYp1a', 'READER'),
  ('Regular Reader', '$2a$10$nALiN6cco3SftvENnV7VO.63vcF20yaQYIuIdb7Q7GnJL/MuosGsG', 'READER');

INSERT INTO items (id, book_id, status, user, place, due_date) VALUES
  (1, 3, 'ON_SHELF', 'Admin', '1-st shelf', null),
  (2, 2, 'ON_HANDS', 'The Great Old Librarian', '2-nd shelf', '2017-06-11'),
  (3, 1, 'ON_HANDS', 'Regular Reader', '4-th shelf', '2017-06-11'),
  (4, 3, 'ON_HANDS', 'Dummy Reader', '5-th shelf', '2017-06-11');

INSERT INTO item_logs (id, item_id, item_status, item_holder, item_due_date, made_by, timestamp) VALUES
  (1, 1, '0', null, null, 'Admin', '2017-06-01 15:19:17.014000'),
  (2, 1, '1', null, null, 'Admin', '2017-06-01 15:19:28.157000'),
  (3, 1, '2', 'Dummy Reader', '2017-06-11', 'Admin', '2017-06-01 15:19:35.447000'),
  (4, 1, '3', null, '2017-06-02', 'Admin', '2017-06-01 15:19:39.077000'),
  (5, 1, '1', null, null, 'Admin', '2017-06-01 15:19:41.039000'),
  (6, 1, '2', 'Regular Reader', '2017-06-11', 'Admin', '2017-06-01 15:19:44.827000'),
  (7, 1, '3', null, '2017-06-02', 'Admin', '2017-06-01 15:19:50.784000'),
  (8, 1, '1', null, null, 'Admin', '2017-06-01 15:19:56.046000'),
  (9, 2, '0', null, null, 'Admin', '2017-06-01 15:20:24.306000'),
  (10, 2, '1', null, null, 'Admin', '2017-06-01 15:20:31.502000'),
  (11, 2, '2', 'The Great Old Librarian', '2017-06-11', 'Admin', '2017-06-01 15:20:35.547000'),
  (12, 3, '0', null, null, 'Admin', '2017-06-01 15:20:49.867000'),
  (13, 3, '1', null, null, 'Admin', '2017-06-01 15:21:02.900000'),
  (14, 3, '2', 'Regular Reader', '2017-06-11', 'Admin', '2017-06-01 15:21:10.501000'),
  (15, 3, '3', null, '2017-06-02', 'Admin', '2017-06-01 15:21:11.901000'),
  (16, 3, '0', null, null, 'Admin', '2017-06-01 15:21:15.417000'),
  (17, 3, '4', null, null, 'Admin', '2017-06-01 15:21:18.910000'),
  (18, 3, '0', null, null, 'Admin', '2017-06-01 15:21:20.881000'),
  (19, 3, '1', null, null, 'Admin', '2017-06-01 15:21:29.898000'),
  (20, 3, '2', 'Regular Reader', '2017-06-11', 'Admin', '2017-06-01 15:21:37.061000'),
  (21, 4, '0', null, null, 'Admin', '2017-06-01 15:21:54.730000'),
  (22, 4, '1', null, null, 'Admin', '2017-06-01 15:22:04.627000'),
  (23, 4, '2', 'Dummy Reader', '2017-06-11', 'Admin', '2017-06-01 15:22:08.362000');

COMMIT;
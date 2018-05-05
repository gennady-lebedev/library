CREATE DATABASE library;

USE library;

CREATE TABLE items (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  optional_comment TEXT,
  last_time_changed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ID)
);

INSERT INTO items (id, name, optional_comment) VALUES
  (1, 'sample', 'sample\'s comment'),
  (2, 'example', 'example\'s comment'),
  (3, 'something', null),
  (4, 'else', null),
  (5, 'foobar', 'foobar\'s comment');
CREATE TABLE person (
  id bigint(20) AUTO_INCREMENT,
  first_name varchar(50) NOT NULL,
  last_name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
);

INSERT INTO person VALUES(null, 'Katherine', 'Li');

INSERT INTO person VALUES(null, 'Hearen', 'Lo');

INSERT INTO person VALUES(null, 'Catie', 'Li');

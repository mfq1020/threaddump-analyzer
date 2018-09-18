CREATE TABLE person (
  id bigint(20) AUTO_INCREMENT,
  first_name varchar(50) NOT NULL,
  last_name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
);

INSERT INTO person VALUES(null, 'Katherine', 'Li');

INSERT INTO person VALUES(null, 'Hearen', 'Lo');

INSERT INTO person VALUES(null, 'Catie', 'Li');

CREATE TABLE s3_info (
  id bigint(20) AUTO_INCREMENT,
  access_key varchar(128) NOT NULL,
  secret_key varchar(128) NOT NULL,
  PRIMARY KEY (id)
);

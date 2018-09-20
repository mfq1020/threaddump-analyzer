DROP TABLE IF EXISTS s3_info;
CREATE TABLE s3_info (
  id bigint(20) AUTO_INCREMENT,
  access_key varchar(128) NOT NULL,
  secret_key varchar(128) NOT NULL,
  PRIMARY KEY (id)
);

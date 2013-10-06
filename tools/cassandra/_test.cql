-- http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_reference/create_keyspace_r.html

-- This does not create replicas on other DCs.
-- CREATE SCHEMA schema1 
-- WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE SCHEMA schema1 
WITH replication = {
	'class' : 'NetworkTopologyStrategy',
	'DC0' : 1,
	'DC1' : 1,
	'DC2' : 1,
	'DC3' : 1 };

USE schema1;

CREATE TABLE users (
		user_id varchar PRIMARY KEY,
		first varchar,
		last varchar,
		age int 
		);

INSERT INTO users (user_id, first, last, age) 
	VALUES ('jsmith', 'John', 'Smith', 42);

-- SELECT * FROM users;

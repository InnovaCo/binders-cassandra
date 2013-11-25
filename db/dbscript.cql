create keyspace binder_test with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

use binder_test;

create table users (
		userId int primary key,
		name text,
		created timestamp
	);


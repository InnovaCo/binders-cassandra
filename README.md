# binders-cassandra

This is a data binder library for Cassandra written in Scala. Please see more information at [binders library page](/InnovaCo/binders)

Cassandra data using [DataStax Java Driver for Cassandra](https://github.com/datastax/java-driver).

## Compilation    

I haven't pushed it into the public artifactory repositaries yet. So it should be used locally. 

To compile and publish into the local repositary:
sbt publish-local

Compiled binders also should be in local repositary.

For the unit tests and sample application working local instance of Cassandra is required. Please see schema in db/dbscript.cql
    
## Requirements

Currently tested and works only with:

* binders-core
* Cassandra 2.0.1 (corresponding driver with prepared statements)
* Scala 2.10
* sbt 0.13

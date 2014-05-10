package eu.inn.binders.cassandra

class NoRowsSelectedException(objectType: String) extends RuntimeException(s"No rows were selected by statement: $objectType")


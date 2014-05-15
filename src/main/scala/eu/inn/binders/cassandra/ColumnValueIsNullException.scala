package eu.inn.binders.cassandra


class ColumnValueIsNullException(val columnName: String) extends RuntimeException("Column " + columnName + " has null value")

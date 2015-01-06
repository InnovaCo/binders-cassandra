package eu.inn.binders.cassandra.internal

import eu.inn.binders.cassandra._

object Helpers {
  def checkIfApplied[A](rows: Rows[_], objectName: String, unbindFunc:() => Option[A]) : IfApplied[A] = {
    if (rows.wasApplied)
      Applied
    else
      if (rows.resultSet.getColumnDefinitions.size > 1)
        NotAppliedExists(
          unbindFunc().getOrElse(
            throw new NoRowsSelectedException(objectName)
          )
      )
      else
        NotApplied
  }
}

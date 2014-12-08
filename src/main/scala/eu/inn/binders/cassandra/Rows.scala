package eu.inn.binders.cassandra

import scala.reflect.runtime.universe._

import com.datastax.driver.core.ResultSet

import eu.inn.binders.naming.Converter


class Rows[C <: Converter : TypeTag](val resultSet: ResultSet) extends eu.inn.binders.core.Deserializer[C] {
  import scala.collection.JavaConversions._

  def hasField(fieldName : scala.Predef.String) = ???

  def iterator(): Iterator[Row[C]] = {
    if (_it != null) {
      val i = _it
      _it = null
      i
    }
    else
      resultSet.iterator().map(r => new Row[C](r))
  }

  // todo: update to resultSet.wasApplied() and remove _it
  // this is temporary implementation, until this commit is released
  // https://github.com/datastax/java-driver/commit/cd843bea8dee74506cd0a2c4753bcd678998b73a
  private var _it: Iterator[Row[C]] = null

  def wasApplied: Boolean = {
    val i = iterator()
    if (i.hasNext) {
      val firstRow = i.next()
      _it = Iterator(firstRow) ++ i
      firstRow.getBoolean("[applied]")
    }
    else {
      true
    }
  }
}

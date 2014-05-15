package eu.inn.binders.cassandra

import scala.reflect.runtime.universe._

import com.datastax.driver.core.ResultSet

import eu.inn.binders.naming.Converter


class Rows[C <: Converter : TypeTag](resultSet: ResultSet) extends eu.inn.binders.core.Rows[Row[C]] {
  type nameConverterType = C

  import scala.collection.JavaConversions._

  def iterator: Iterator[Row[C]] = resultSet.iterator().map(r => new Row[C](r))
}

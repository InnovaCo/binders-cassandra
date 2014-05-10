package eu.inn.binders.cassandra

import com.datastax.driver.core.ResultSet
import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._

class Rows[C <: Converter : TypeTag](resultSet: ResultSet) extends eu.inn.binders.core.Rows[Row[C]] {
  type nameConverterType = C

  import scala.collection.JavaConversions._

  def iterator: Iterator[Row[C]] = resultSet.iterator().map(r => new Row[C](r))
}

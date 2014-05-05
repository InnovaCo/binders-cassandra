package eu.inn.binders.cassandra

import com.datastax.driver.core.ResultSet


class Rows(resultSet: ResultSet) extends eu.inn.binders.core.Rows[Row] {

  import scala.collection.JavaConversions._

  def iterator: Iterator[Row] = resultSet.iterator().map(r => new Row(r))
}

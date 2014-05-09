package eu.inn.binders.cassandra

import com.datastax.driver.core.{PreparedStatement, ResultSet, BoundStatement, Session}
import scala.concurrent.{Promise, Future}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._


class Query[C <: Converter : TypeTag](val session: Session, val preparedStatement: PreparedStatement) extends eu.inn.binders.core.Query[Statement[C]] {
  //type C = C

  def this(session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def createStatement(): Statement[C] = new Statement[C](session, new BoundStatement(preparedStatement))
}

package eu.inn.binders.cassandra

import scala.reflect.runtime.universe._

import com.datastax.driver.core.{PreparedStatement, BoundStatement, Session}

import eu.inn.binders.naming.Converter


class Query[C <: Converter : TypeTag](val session: Session, val preparedStatement: PreparedStatement) {
  //type C = C

  def this(session: Session, queryString: String) = this(session, session.prepare(queryString))

  def createStatement(): BoundStatementWrapper[C] = new BoundStatementWrapper[C](session, new BoundStatement(preparedStatement))
}

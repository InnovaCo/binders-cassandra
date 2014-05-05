package eu.inn.binders.cassandra

import com.datastax.driver.core.{PreparedStatement, BoundStatement, Session}


class Query(val session: Session, val preparedStatement: PreparedStatement) extends eu.inn.binders.core.Query[Rows, Statement]{
  def this (session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def executeStatement(statement: Statement): Rows = new Rows(session.execute(statement.boundStatement))

  override def createStatement(): Statement = new Statement(new BoundStatement(preparedStatement))
}

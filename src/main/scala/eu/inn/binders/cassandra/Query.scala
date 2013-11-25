package eu.inn.binders.cassandra

import com.datastax.driver.core.{BoundStatement, Session}


class Query(val session: Session, val queryString: String) extends eu.inn.binders.core.Query[Rows, Statement]{
  val preparedStatement = session.prepare(queryString)

  def bindAndExecute(f: (Statement) => Unit): Rows = {
    val boundStatement = new BoundStatement(preparedStatement)
    val statement = new Statement(boundStatement)
    f(statement)
    new Rows(session.execute(boundStatement))
  }
}

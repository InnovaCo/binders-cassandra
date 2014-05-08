package eu.inn.binders.cassandra

import com.datastax.driver.core.{PreparedStatement, BoundStatement, Session}
import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._


class Query[C <: Converter: TypeTag](val session: Session, val preparedStatement: PreparedStatement) extends eu.inn.binders.core.Query[Rows[C], Statement[C]] {
  //type C = C

  def this(session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def executeStatement(statement: Statement[C]): Rows[C] = new Rows[C](session.execute(statement.boundStatement))

  override def createStatement(): Statement[C] = new Statement[C](new BoundStatement(preparedStatement))
}

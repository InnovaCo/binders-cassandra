package eu.inn.binders.cassandra

import com.datastax.driver.core.{PreparedStatement, ResultSet, BoundStatement, Session}
import scala.concurrent.{Promise, Future}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._


class AsyncQuery[C <: Converter : TypeTag](val session: Session, val preparedStatement: PreparedStatement) extends eu.inn.binders.core.Query[Future[Rows[C]], Statement[C]] {
  //type C = C

  def this(session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def executeStatement(statement: Statement[C]): Future[Rows[C]] = {
    val futureResult = session.executeAsync(statement.boundStatement)
    val promise = Promise[Rows[C]]
    val conv = new FutureConverter(promise)
    Futures.addCallback(futureResult, conv)
    promise.future
  }

  override def createStatement(): Statement[C] = new Statement[C](new BoundStatement(preparedStatement))

  private class FutureConverter(promise: Promise[Rows[C]]) extends FutureCallback[ResultSet] {
    override def onFailure(t: Throwable): Unit = promise.failure(t)

    override def onSuccess(result: ResultSet): Unit = promise.success(new Rows(result))
  }

}

package eu.inn.binders.cassandra

import com.datastax.driver.core.{PreparedStatement, ResultSet, BoundStatement, Session}
import scala.concurrent.{Promise, Future}
import com.google.common.util.concurrent.{FutureCallback, Futures}


class AsyncQuery(val session: Session, val preparedStatement: PreparedStatement) extends eu.inn.binders.core.Query[Future[Rows], Statement]{
  def this (session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def executeStatement(statement: Statement): Future[Rows] = {
    val futureResult = session.executeAsync(statement.boundStatement)
    val promise = Promise[Rows]
    val conv = new FutureConverter(promise)
    Futures.addCallback(futureResult, conv)
    promise.future
  }

  override def createStatement(): Statement = new Statement(new BoundStatement(preparedStatement))

	private class FutureConverter(promise: Promise[Rows]) extends FutureCallback[ResultSet] {
		override def onFailure(t: Throwable): Unit = promise.failure(t)
		override def onSuccess(result: ResultSet): Unit = promise.success(new Rows(result))
	}
}

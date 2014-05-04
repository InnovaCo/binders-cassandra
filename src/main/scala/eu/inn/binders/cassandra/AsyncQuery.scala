package eu.inn.binders.cassandra

import com.datastax.driver.core.{ResultSet, BoundStatement, Session}
import scala.concurrent.{ExecutionContext, Promise, Future}
import com.google.common.util.concurrent.{FutureCallback, Futures}


class AsyncQuery(val session: Session, val queryString: String) extends eu.inn.binders.core.Query[Future[Rows], Statement]{
  val preparedStatement = session.prepare(queryString)

	private class FutureConverter(promise: Promise[ResultSet]) extends FutureCallback[ResultSet] {
		override def onFailure(t: Throwable): Unit = promise.failure(t)
		override def onSuccess(result: ResultSet): Unit = promise.success(result)
	}

  def bindAndExecute(f: (Statement) => Unit): Future[Rows] = {
		import ExecutionContext.Implicits.global

    val boundStatement = new BoundStatement(preparedStatement)
    val statement = new Statement(boundStatement)
    f(statement)
		val futureResult = session.executeAsync(boundStatement)
		val promise = Promise[ResultSet]
		val conv = new FutureConverter(promise)
		Futures.addCallback(futureResult, conv)
		promise.future map(new Rows(_))
  }
}

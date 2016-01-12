package eu.inn.binders.cassandra

import scala.concurrent.{Promise, Future}
import scala.reflect.runtime.universe._

import com.datastax.driver.core.{Statement ⇒ DriverStatement, _}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import org.slf4j.LoggerFactory

import eu.inn.binders.naming.Converter

/**
  * statement have to be protected in general
  */
abstract class AbstractStatement[C <: Converter : TypeTag, S <: DriverStatement](val session: Session, protected val statement: S) {

  protected val logger = LoggerFactory.getLogger(getClass)

  def execute(): Future[Rows[C]] = {
    if (logger.isTraceEnabled) {
      logger.trace(queryString().trim)
    }

    val promise = Promise[Rows[C]]()
    Futures.addCallback(session.executeAsync(statement), new FutureConverter(promise))
    promise.future
  }

  def asNonIdempotent(): AbstractStatement[C, S] = {
    statement.setIdempotent(false)
    this
  }

  def asIdempotent(): AbstractStatement[C, S] = {
    statement.setIdempotent(true)
    this
  }

  def withConsistency(consistency: ConsistencyLevel): AbstractStatement[C, S] = {
    if (consistency != ConsistencyLevel.LOCAL_SERIAL && consistency != ConsistencyLevel.SERIAL) {
      statement.setConsistencyLevel(consistency)
    } else {
      statement.setSerialConsistencyLevel(consistency)
    }
    this
  }

  def withTimestamp(timestamp: Long): AbstractStatement[C, S] = {
    statement.setDefaultTimestamp(timestamp)
    this
  }

  protected def queryString(): String

  private class FutureConverter(promise: Promise[Rows[C]]) extends FutureCallback[ResultSet] {
    override def onFailure(t: Throwable) {
      promise.failure(t)
    }

    override def onSuccess(result: ResultSet) {
      promise.success(new Rows(result))
    }
  }
}
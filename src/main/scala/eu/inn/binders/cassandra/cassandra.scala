package eu.inn.binders

import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros
import scala.reflect.runtime.universe._

import com.datastax.driver.core.BatchStatement

import eu.inn.binders.cassandra.internal.CqlMacro
import eu.inn.binders.naming.Converter


package object cassandra {

  implicit class CqlContext(val sc: StringContext) extends AnyVal {
    def cql[C <: Converter : SessionQueryCache](args: Any*): BoundStatementWrapper[C] = macro CqlMacro.cql[C]
  }

  implicit class StatementOps[S <: StatementWrapper[_, _]](val stmt: S) extends AnyVal {
    def one[O](implicit executor: ExecutionContext): Future[O] = macro CqlMacro.one[S, O]

    def oneApplied[O](implicit executor: ExecutionContext): Future[IfApplied[O]] = macro CqlMacro.oneApplied[S, O]

    def oneOption[O](implicit executor: ExecutionContext): Future[Option[O]] = macro CqlMacro.oneOption[S, O]

    def all[O](implicit executor: ExecutionContext): Future[Iterator[O]] = macro CqlMacro.all[S, O]
  }

  implicit def convertFutureToUnit[R <: Rows[_]](f: Future[R])(implicit executor: ExecutionContext): Future[Unit] = f.map(_ ⇒ {})

  def batch[C  <: Converter : SessionQueryCache : TypeTag](wrappers: BoundStatementWrapper[_] *)(implicit cache: SessionQueryCache[_]): BatchStatementWrapper[C] = {
    batchWithType[C](BatchStatement.Type.LOGGED, wrappers: _*)
  }

  def unloggedBatch[C  <: Converter : SessionQueryCache : TypeTag](wrappers: BoundStatementWrapper[_] *)(implicit cache: SessionQueryCache[_]): BatchStatementWrapper[C] = {
    batchWithType[C](BatchStatement.Type.UNLOGGED, wrappers: _*)
  }

  def counterBatch[C  <: Converter : SessionQueryCache : TypeTag](wrappers: BoundStatementWrapper[_] *)(implicit cache: SessionQueryCache[_]): BatchStatementWrapper[C] = {
    batchWithType[C](BatchStatement.Type.COUNTER, wrappers: _*)
  }

  private def batchWithType[C  <: Converter : SessionQueryCache : TypeTag](batchType: BatchStatement.Type, wrappers: BoundStatementWrapper[_] *)(implicit cache: SessionQueryCache[_]): BatchStatementWrapper[C] = {
    val statements = wrappers.map(_.boundStatement)
    new BatchStatementWrapper[C](cache.session, batchType, statements: _*)
  }
}

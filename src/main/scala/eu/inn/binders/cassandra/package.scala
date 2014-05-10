package eu.inn.binders

import eu.inn.binders.cassandra.internal.CqlMacro
import language.experimental.macros
import eu.inn.binders.naming.Converter
import scala.concurrent.{ExecutionContext, Future}

package object cassandra {

  implicit class CqlContext(val sc: StringContext) {
    def cql[C <: Converter](args: Any*)(implicit sessionQueryCache: SessionQueryCache[C]): Statement[C] = macro CqlMacro.cql[C]
  }

  implicit class StatementOps[S <: Statement[_]](val stmt: S) {
    def one[O](implicit executor: ExecutionContext): Future[O] = macro CqlMacro.one[S, O]

    def oneOption[O](implicit executor: ExecutionContext): Future[Option[O]] = macro CqlMacro.oneOption[S, O]

    def all[O](implicit executor: ExecutionContext): Future[Iterator[O]] = macro CqlMacro.all[S, O]
  }

  implicit def convertFutureToUnit[R <: Rows[_]](f: Future[R])(implicit executor: ExecutionContext): Future[Unit] = f.map(x => {})
}

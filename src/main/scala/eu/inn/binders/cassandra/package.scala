package eu.inn.binders

import eu.inn.binders.cassandra.internal.CqlMacro
import language.experimental.macros
import eu.inn.binders.naming.Converter
import scala.concurrent.Future

package object cassandra {
  implicit class CqlContext(val sc: StringContext) {
    def cql[C <: Converter](args: Any *)(implicit sessionQueryCache: SessionQueryCache[C]) = macro CqlMacro.cql[C]
  }
}

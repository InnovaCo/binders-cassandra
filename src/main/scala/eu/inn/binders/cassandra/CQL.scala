package eu.inn.binders.cassandra

import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._

object CQL {
  def apply[C <: Converter](query: String)(implicit tag: TypeTag[C], sessionStatementCache: SessionQueryCache[C]): AsyncQuery[C] = {
    sessionStatementCache.getOrCreate(query, () => {
      new AsyncQuery[C](sessionStatementCache.session, query)
    })
  }
}

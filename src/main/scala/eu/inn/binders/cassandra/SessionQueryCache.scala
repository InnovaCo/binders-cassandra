package eu.inn.binders.cassandra

import com.datastax.driver.core.Session
import com.google.common.cache.{Cache, CacheBuilder}
import java.util.concurrent.Callable
import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._

class SessionQueryCache[C <: Converter : TypeTag](val session: Session) {

  def createQuery(query: String): Query[C] = getOrCreate(query, () => {
    new Query[C](session, query)
  })

  private val cache: Cache[String, Query[C]] =
    CacheBuilder.newBuilder().weakKeys().build()

  private def getOrCreate(query: String, createStatement: () => Query[C]): Query[C] = {
    cache.get(query, new Loader(query))
  }

  private class Loader(query: String) extends Callable[Query[C]] {
    override def call(): Query[C] = new Query[C](session, query)
  }

}

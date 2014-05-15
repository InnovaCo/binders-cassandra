package eu.inn.binders.cassandra

import java.util.concurrent.Callable
import scala.reflect.runtime.universe._

import com.datastax.driver.core.Session
import com.google.common.cache.{Cache, CacheBuilder}

import eu.inn.binders.naming.Converter


class SessionQueryCache[C <: Converter : TypeTag](val session: Session) {

  def createQuery(query: String): Query[C] =
    cache.get(query, new Loader(query))

  private val cache: Cache[String, Query[C]] =
    CacheBuilder.newBuilder().weakKeys().build()

  private class Loader(query: String) extends Callable[Query[C]] {
    override def call(): Query[C] = new Query[C](session, query)
  }

}

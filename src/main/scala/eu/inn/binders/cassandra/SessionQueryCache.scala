package eu.inn.binders.cassandra

import com.datastax.driver.core.Session
import com.google.common.cache.{Cache, CacheBuilder}
import java.util.concurrent.Callable
import eu.inn.binders.naming.Converter
import scala.reflect.runtime.universe._

class SessionQueryCache[C <: Converter : TypeTag](val session: Session) {
  type nameConverterType = C
  val cache: Cache[String, AsyncQuery[nameConverterType]] =
    CacheBuilder.newBuilder().weakKeys().build()

  def getOrCreate(query: String, createStatement: () => AsyncQuery[nameConverterType]): AsyncQuery[nameConverterType] = {
    cache.get(query, new Loader(query))
  }

  private class Loader(query: String) extends Callable[AsyncQuery[nameConverterType]] {
    override def call(): AsyncQuery[nameConverterType] = new AsyncQuery[nameConverterType](session, query)
  }

}

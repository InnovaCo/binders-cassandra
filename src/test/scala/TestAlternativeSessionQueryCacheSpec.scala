import java.util.concurrent.Callable

import com.datastax.driver.core._
import com.google.common.cache.{CacheBuilder, Cache}
import eu.inn.binders.cassandra._
import eu.inn.binders.naming.{SnakeCaseToCamelCaseConverter, Converter, PlainConverter}
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.runtime.universe._


class AlternativeQuery[C <: Converter : TypeTag](session: Session, preparedStatement: PreparedStatement)
  extends eu.inn.binders.cassandra.Query[C](session, preparedStatement) {
  def this(session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def createStatement(): eu.inn.binders.cassandra.Statement[C] = new AlternativeStatement[C](session, new BoundStatement(preparedStatement))
}

class AlternativeSessionQueryCache[C <: Converter : TypeTag](session: Session) extends SessionQueryCache[C](session) {
  override protected def newQuery(query: String) = new AlternativeQuery[C](session, query)
}

class AlternativeStatement[C <: Converter : TypeTag](session: Session, boundStatement: BoundStatement)
  extends eu.inn.binders.cassandra.Statement[C](session, boundStatement) {
}

class TestAlternativeSessionQueryCacheSpec extends FlatSpec with Matchers {

  import scala.concurrent.ExecutionContext.Implicits.global

  "cql... " should " allow to use alternative SessionQueryCache implementation " in {
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect("binder_test")
    implicit var sessionQueryCache = new AlternativeSessionQueryCache[SnakeCaseToCamelCaseConverter](session)
    val userCql = cql"select userId,name,created from users where userid=1".execute()
    Await.result(userCql, 20 seconds)
  }
}
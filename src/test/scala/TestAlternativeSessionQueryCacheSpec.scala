import com.datastax.driver.core._
import eu.inn.binders.cassandra._
import eu.inn.binders.naming.{Converter, SnakeCaseToCamelCaseConverter}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.runtime.universe._


class AlternativeQuery[C <: Converter : TypeTag](session: Session, preparedStatement: PreparedStatement)
  extends eu.inn.binders.cassandra.Query[C](session, preparedStatement) {
  def this(session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def createStatement(): eu.inn.binders.cassandra.Statement[C] = new AlternativeStatement[C](session, new BoundStatement(preparedStatement))
}

class AlternativeSessionQueryCache[C <: Converter : TypeTag](session: Session) extends GuavaSessionQueryCache[C](session) {
  override protected def newQuery(query: String) = new AlternativeQuery[C](session, query)
}

class AlternativeStatement[C <: Converter : TypeTag](session: Session, boundStatement: BoundStatement)
  extends eu.inn.binders.cassandra.Statement[C](session, boundStatement) {
}

class TestAlternativeSessionQueryCacheSpec extends FlatSpec with Matchers {
  "cql... " should " allow to use alternative SessionQueryCache implementation " in {
    Cassandra.start
    implicit var sessionQueryCache = new AlternativeSessionQueryCache[SnakeCaseToCamelCaseConverter](Cassandra.session)
    val userCql = cql"select userId,name,created from users where userid=1".execute()
    Await.result(userCql, 20 seconds)
  }
}
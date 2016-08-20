import com.datastax.driver.core.Session
import eu.inn.binders.cassandra.Dynamic
import org.scalatest.{FlatSpec, Matchers}
import eu.inn.binders.cassandra._
import eu.inn.binders.naming.LowercaseConverter
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext

class TestDynamicSpec extends FlatSpec with Matchers with SessionFixture with ScalaFutures {
  import ExecutionContext.Implicits.global

  case class User(userId: Int, name: String, created: java.util.Date)
  case class UserName(name: String)

  "cql...execute " should " be able to execute dynamic cql with parameters " in {
    val d1 = Dynamic("select")
    val d2 = Dynamic("userId,")
    val d3 = Dynamic("name,created")
    val user = await(cql"$d1 $d2 $d3 from users ${Dynamic("where userid=?")}".bindArgs(11).execute()
    ).unbind[Seq[User]].head

    assert(user.userId == 11)
    assert(user.name == "alla")
    assert(user.created == yesterday)
  }

  "cql...execute " should " be able to cache and execute dynamic cql with parameters " in {
    val user1 = dynamicCql(11).futureValue.head

    assert(user1.userId == 11)
    assert(user1.name == "alla")
    assert(user1.created == yesterday)

    val user2 = dynamicCql(10).futureValue.head

    assert(user2.userId == 10)
    assert(user2.name == "maga")
    assert(user2.created == yesterday)
  }

  def dynamicCql(userId: Int) = {
    val d1 = Dynamic("select")
    val d2 = Dynamic("userId,")
    val d3 = Dynamic("name,created")
    val cql1 = cql"$d1 $d2 $d3 from users ${Dynamic("where userid=?")}"
    cql1.bindArgs(userId).all[User].map(_.toSeq)
  }

  "SessionQueryCache " should " return cached statement " in {
    val s1 = "select userId from users where userid=?"
    val s2 = "select userId from users where userid=?"
    val q1 = sessionQueryCache.createQuery(s1)
    val q2 = sessionQueryCache.createQuery(s2)
    assert(q1 == q2)
  }
}


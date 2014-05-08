import eu.inn.binders.cassandra.AsyncQuery
import eu.inn.binders.naming.PlainConverter
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent._
import scala.concurrent.duration._

class TestAsyncQuerySpec extends FlatSpec with Matchers with SessionFixture {
  import ExecutionContext.Implicits.global

  case class User(userId: Int, name: String, created: java.util.Date)

  "AsyncQuery " should " be able to execute command " in {
    val stmt = new AsyncQuery[PlainConverter](session, "delete from users where userid=12")
    Await.result(stmt.execute(), 10 seconds)
  }

  "AsyncQuery " should " be able to execute with parameters " in {
    val stmt = new AsyncQuery[PlainConverter](session, "delete from users where userid=?")
    Await.result(stmt.execute(12), 10 seconds)
  }

  "AsyncQuery " should " be able to select rows " in {
    val stmt = new AsyncQuery[PlainConverter](session, "select userId,name,created from users where userid in (10,11)")
    val usersFuture = stmt.execute().map(_.unbindAll[User].toSeq)
    val users = Await.result(usersFuture, 10 seconds)
    if (users.length != 2) {
      println("!!!! Users = ")
      println(users)
    }
    assert(users.length == 2)
  }
}
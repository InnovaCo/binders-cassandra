import eu.inn.binders.cassandra.AsyncQuery
import org.scalatest.{FlatSpec, Matchers}
import com.datastax.driver.core.Cluster
import scala.concurrent._
import scala.concurrent.duration._

class TestAsyncQuerySpec extends FlatSpec with Matchers {
  import ExecutionContext.Implicits.global

  def fixture = new {
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect("binder_test")

    val yesterday = {
      import java.util._
      val cal = Calendar.getInstance()
      cal.setTime(new Date())
      cal.add(Calendar.DATE, -11)
      cal.getTime()
    }

    val insert = new AsyncQuery(session, "insert into users(userId, name, created) values (10,'maga', ?)")
    Await.result(insert.execute(yesterday), 10 seconds)

    val insert2 = new AsyncQuery(session, "insert into users(userId, name, created) values (11,'alla', ?)")
    Await.result(insert2.execute(yesterday), 10 seconds)
  }

  case class User(userId: Int, name: String, created: java.util.Date)

  "AsyncQuery " should " be able to execute command " in {
    val f = fixture
    val stmt = new AsyncQuery(f.session, "delete from users where userid=10")
    Await.result(stmt.execute(), 10 seconds)
  }

  "AsyncQuery " should " be able to execute with parameters " in {
    val f = fixture
    val stmt = new AsyncQuery(f.session, "delete from users where userid=?")
    Await.result(stmt.execute(10), 10 seconds)
  }

  "AsyncQuery " should " be able to select rows " in {
    val f = fixture
    val stmt = new AsyncQuery(f.session, "select userId,name,created from users where userid in (10,11)")
    val usersFuture = stmt.execute().map(_.unbindAll[User])
    val users = Await.result(usersFuture, 10 seconds)
    assert(users.length == 2)
  }
}
import eu.inn.binders.cassandra.Query
import org.scalatest.{FlatSpec,Matchers}
import eu.inn.binders._
import com.datastax.driver.core.{Session, Cluster, Row, BoundStatement}

class TestQuerySpec extends FlatSpec with Matchers {

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

    val insert = new Query(session, "insert into users(userId, name, created) values (10,'maga', ?)")
    insert.execute(yesterday)

    val insert2 = new Query(session, "insert into users(userId, name, created) values (11,'alla', ?)")
    insert2.execute(yesterday)
  }

  case class User(userId: Int, name: String, created: java.util.Date)

  "Query " should " be able to execute command " in {
    val f = fixture
    val stmt = new Query(f.session, "delete from users where userid=10")
    stmt.execute()
  }

  "Query " should " be able to execute with parameters " in {
    val f = fixture
    val stmt = new Query(f.session, "delete from users where userid=?")
    stmt.execute(10)
  }

  "Query " should " be able to execute with 2 primitive parameters " in {
    val f = fixture
    val stmt = new Query(f.session, "delete from users where userid in(?,?)")
    stmt.execute(10, 11)
  }

  "Query " should " be able to select one row " in {
    val f = fixture
    val stmt = new Query(f.session, "select userId,name,created from users where userid=10")
    val user = stmt.execute().unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 10)
    assert(user.get.name == "maga")
    assert(user.get.created == f.yesterday)
  }

  "Query " should " be able to select one row with parameters " in {
    val f = fixture
    val stmt = new Query(f.session, "select userId,name,created from users where userid=?")
    val user = stmt.execute(11).unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 11)
    assert(user.get.name == "alla")
    assert(user.get.created == f.yesterday)
  }

  "Query " should " be able to select two rows with 2 plain parameters " in {
    val f = fixture
    val stmt = new Query(f.session, "select userId,name,created from users where userid in(?,?)")
    val users = stmt.execute(10, 11).unbindAll[User].toSeq
    assert(users.length == 2)
  }

  "Query " should " be able to select rows " in {
    val f = fixture
    val stmt = new Query(f.session, "select userId,name,created from users where userid in (10,11)")
    val users = stmt.execute().unbindAll[User]

    assert(users.length == 2)
  }

  "Query " should " be able to select 0 rows " in {
    val f = fixture
    val stmt = new Query(f.session, "select userId,name,created from users where userid in (12,13)")
    val users = stmt.execute().unbindAll[User]

    assert(users.length == 0)
  }

  /*
  this doesn't work, because :userId can't be set as a list
  case class UserListParams(users: Array[Int])
  "Query " should " be able to select rows with List parameter" in {
    val f = fixture
    val stmt = new Query(f.session, "select userId,name from users where userid in (:users)")

    val params = UserListParams(Array(10,11))
    val users = stmt.selectWith[UserListParams,User](params)

    assert(users.length == 2)
  }*/
}
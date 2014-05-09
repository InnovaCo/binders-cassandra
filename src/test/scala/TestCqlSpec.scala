import eu.inn.binders.cassandra.{Rows, CqlContext}
import eu.inn.binders.naming.{PlainConverter, Converter}
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._


class TestCqlSpec extends FlatSpec with Matchers with SessionFixture {

  case class User(userId: Int, name: String, created: java.util.Date)

  "Query " should " be able to execute command with StringContext parameters" in {
    val userId=12
    await(cql"delete from users where userid=$userId".executeStatement())
  }

  "Query " should " be able to execute command " in {
    await(cql"delete from users where userid=12".executeStatement())
  }

  "Query " should " be able to execute with parameters " in {
    await(cql"delete from users where userid=?".bindParameter(0,12).executeStatement())
  }

  "Query " should " be able to execute with 2 primitive parameters " in {
    await(cql"delete from users where userid in(?,?)".bindArgs(12, 13).executeStatement())
  }

  "Query " should " be able to select one row " in {
    val user = await(
      cql"select userId,name,created from users where userid=10".executeStatement()
    ).unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 10)
    assert(user.get.name == "maga")
    assert(user.get.created == yesterday)
  }

  "Query " should " be able to select one row with parameters " in {
    val user = await(
      cql"select userId,name,created from users where userid=?".bindArgs(11).executeStatement()
    ).unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 11)
    assert(user.get.name == "alla")
    assert(user.get.created == yesterday)
  }

  "Query " should " be able to select two rows with 2 plain parameters " in {
    val users = await(
      cql"select userId,name,created from users where userid in(?,?)".bindArgs(10, 11).executeStatement()
    ).unbindAll[User]

    assert(users.length == 2)
  }

  "Query " should " be able to select rows " in {
    val users = await(
      cql"select userId,name,created from users where userid in (10,11)".executeStatement()
    ).unbindAll[User]
    assert(users.length == 2)
  }

  "Query " should " be able to select 0 rows " in {
    val users = await(
      cql"select userId,name,created from users where userid in (12,13)".executeStatement()
    ).unbindAll[User]
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
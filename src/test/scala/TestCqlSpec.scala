import eu.inn.binders.cassandra.{Rows, CQL}
import eu.inn.binders.naming.Converter
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.reflect.runtime.universe._

class TestCqlSpec extends FlatSpec with Matchers with SessionFixture {

  def await[C <: Converter : TypeTag](r: Future[Rows[C]]): Rows[C] = Await.result(r, 20 seconds)

  case class User(userId: Int, name: String, created: java.util.Date)

  "Query " should " be able to execute command " in {
    await(CQL("delete from users where userid=12").execute())
  }

  "Query " should " be able to execute with parameters " in {
    await(CQL("delete from users where userid=?").execute(12))
  }

  "Query " should " be able to execute with 2 primitive parameters " in {
    await(CQL("delete from users where userid in(?,?)").execute(12, 13))
  }

  "Query " should " be able to select one row " in {
    val user = await(
      CQL("select userId,name,created from users where userid=10").execute()
    ).unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 10)
    assert(user.get.name == "maga")
    //assert(user.get.created == yesterday)
  }

  "Query " should " be able to select one row with parameters " in {
    val user = await(
      CQL("select userId,name,created from users where userid=?").execute(11)
    ).unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 11)
    assert(user.get.name == "alla")
    //assert(user.get.created == yesterday)
  }

  "Query " should " be able to select two rows with 2 plain parameters " in {
    val users = await(
      CQL("select userId,name,created from users where userid in(?,?)").execute(10, 11)
    ).unbindAll[User]

    assert(users.length == 2)
  }

  "Query " should " be able to select rows " in {
    val users = await(
      CQL("select userId,name,created from users where userid in (10,11)").execute()
    ).unbindAll[User]
    assert(users.length == 2)
  }

  "Query " should " be able to select 0 rows " in {
    val users = await(
      CQL("select userId,name,created from users where userid in (12,13)").execute()
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
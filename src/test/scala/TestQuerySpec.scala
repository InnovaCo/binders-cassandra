import eu.inn.binders.cassandra.Query
import eu.inn.binders.naming.PlainConverter
import org.scalatest.{FlatSpec, Matchers}
import eu.inn.binders._

class TestQuerySpec extends FlatSpec with Matchers with SessionFixture {

  case class User(userId: Int, name: String, created: java.util.Date)

  "Query " should " be able to execute command " in {
    val stmt = new Query[PlainConverter](session, "delete from users where userid=12").createStatement
    stmt.execute
  }

  "Query " should " be able to execute with parameters " in {

    val stmt = new Query[PlainConverter](session, "delete from users where userid=?").createStatement
    stmt.bindArgs(12).execute
  }

  "Query " should " be able to execute with 2 primitive parameters " in {

    val stmt = new Query[PlainConverter](session, "delete from users where userid in(?,?)").createStatement
    stmt.bindArgs(12, 13).execute
  }

  "Query " should " be able to select one row " in {

    val stmt = new Query[PlainConverter](session, "select userId,name,created from users where userid=10").createStatement
    val user = await(stmt.execute).unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 10)
    assert(user.get.name == "maga")
    //assert(user.get.created == yesterday)
  }

  "Query " should " be able to select one row with parameters " in {

    val stmt = new Query[PlainConverter](session, "select userId,name,created from users where userid=?").createStatement
    val user = await(stmt.bindArgs(11).execute).unbindOne[User]

    assert(user.isDefined)
    assert(user.get.userId == 11)
    assert(user.get.name == "alla")
    //assert(user.get.created == yesterday)
  }

  "Query " should " be able to select two rows with 2 plain parameters " in {

    val stmt = new Query[PlainConverter](session, "select userId,name,created from users where userid in(?,?)").createStatement
    val users = await(stmt.bindArgs(10, 11).execute).unbindAll[User].toSeq
    assert(users.length == 2)
  }

  "Query " should " be able to select rows " in {

    val stmt = new Query[PlainConverter](session, "select userId,name,created from users where userid in (10,11)").createStatement
    val users = await(stmt.execute).unbindAll[User]

    assert(users.length == 2)
  }

  "Query " should " be able to select 0 rows " in {

    val stmt = new Query[PlainConverter](session, "select userId,name,created from users where userid in (12,13)").createStatement
    val users = await(stmt.execute).unbindAll[User]

    assert(users.length == 0)
  }

  /*
  this doesn't work, because :userId can't be set as a list
  case class UserListParams(users: Array[Int])
  "Query " should " be able to select rows with List parameter" in {
    
    val stmt = new Query(session, "select userId,name from users where userid in (:users)")

    val params = UserListParams(Array(10,11))
    val users = stmt.selectWith[UserListParams,User](params)

    assert(users.length == 2)
  }*/
}
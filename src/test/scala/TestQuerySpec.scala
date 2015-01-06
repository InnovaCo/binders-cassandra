import com.datastax.driver.core.BatchStatement
import eu.inn.binders.cassandra.Query
import eu.inn.binders.naming.{LowercaseConverter, PlainConverter}
import org.scalatest.{FlatSpec, Matchers}
import eu.inn.binders._

class TestQuerySpec extends FlatSpec with Matchers with SessionFixture {

  case class User(userId: Int, name: String, created: java.util.Date)

  "Query " should " be able to execute command " in {
    val stmt = new Query[PlainConverter](session, "delete from users where userid=12").createStatement()
    stmt.execute
  }

  "Query " should " be able to execute with parameters " in {

    val stmt = new Query[PlainConverter](session, "delete from users where userid=?").createStatement()
    stmt.bindArgs(12).execute
  }

  "Query " should " be able to execute with 2 primitive parameters " in {

    val stmt = new Query[PlainConverter](session, "delete from users where userid in(?,?)").createStatement()
    stmt.bindArgs(12, 13).execute
  }

  "Query " should " be able to select one row " in {

    val stmt = new Query[LowercaseConverter](session, "select userId,name,created from users where userid=10").createStatement()
    val user = await(stmt.execute()).unbind[Seq[User]].head

    assert(user.userId == 10)
    assert(user.name == "maga")
    //assert(user.get.created == yesterday)
  }

  "Query " should " be able to select one row with parameters " in {

    val stmt = new Query[LowercaseConverter](session, "select userId,name,created from users where userid=?").createStatement()
    val user = await(stmt.bindArgs(11).execute).unbind[Seq[User]].head

    assert(user.userId == 11)
    assert(user.name == "alla")
    //assert(user.get.created == yesterday)
  }

  "Query " should " be able to select two rows with 2 plain parameters " in {

    val stmt = new Query[LowercaseConverter](session, "select userId,name,created from users where userid in(?,?)").createStatement()
    val users = await(stmt.bindArgs(10, 11).execute).unbind[Seq[User]]
    assert(users.length == 2)
  }

  "Query " should " be able to select rows " in {

    val stmt = new Query[LowercaseConverter](session, "select userId,name,created from users where userid in (10,11)").createStatement()
    val users = await(stmt.execute()).unbind[Seq[User]]

    assert(users.length == 2)
  }

  "Query " should " be able to select 0 rows " in {

    val stmt = new Query[LowercaseConverter](session, "select userId,name,created from users where userid in (12,13)").createStatement()
    val users = await(stmt.execute()).unbind[Seq[User]]

    assert(users.length == 0)
  }

  "Batch query " should " execute in batch using prepared statements" in {
    val stmt1 = new Query[PlainConverter](session, "delete from users where userid=12").createStatement()
    val stmt2 = new Query[PlainConverter](session, "delete from users where userid=13").createStatement()

    val bs = new BatchStatement()
    bs.add(stmt1.boundStatement)
    bs.add(stmt2.boundStatement)
    session.execute(bs)
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
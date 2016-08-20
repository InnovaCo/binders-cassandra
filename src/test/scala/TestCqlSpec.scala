import java.util.Date

import scala.util.Random

import com.datastax.driver.core.BatchStatement
import eu.inn.binders.cassandra._
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._


class TestCqlSpec extends FlatSpec with Matchers with SessionFixture {

  import ExecutionContext.Implicits.global

  case class User(userId: Int, name: String, created: java.util.Date)
  case class UserName(name: String)

  "cql...one " should " select one row with parameters " in {
    val userId = 11
    val user =
      await(cql"select userId,name,created from users where userid=$userId".one[User])

    assert(user.userId == 11)
    assert(user.name == "alla")
    assert(user.created == yesterday)
  }

  def selectUser(userId: Int) = cql"select userId,name,created from users where userid=$userId".one[User]

  "cql...one " should " select one row with parameters (2)" in {
    val user = await(selectUser(11))
  }

  "cql...oneOption " should " return Some() if row is found" in {
    val userId = 11
    val user =
      await(cql"select userId,name,created from users where userid=$userId".oneOption[User])

    assert(user.isDefined)
    assert(user.get.userId == 11)
    assert(user.get.name == "alla")
    assert(user.get.created == yesterday)
  }

  "cql...oneOption " should " return None if no rows were found" in {
    val userId = 111
    val userO =
      await(cql"select userId,name,created from users where userid=$userId".oneOption[User])

    assert(userO == None)
  }

  "cql...all " should " be able to select rows " in {
    val userId1 = 10
    val userId2 = 11
    val users = await(
      cql"select userId,name,created from users where userid in ($userId1,$userId2)".all[User]
    )
    assert(users.length == 2)
  }

  "cql...one " should " throw NoRowsSelectedException if there were no rows selected " in {
    val userId = 111
    intercept[NoRowsSelectedException] {
      await(cql"select userId,name,created from users where userid=$userId".one[User])
    }
  }

  "cql...execute " should " be able to execute command with StringContext parameters" in {
    val userId = 12
    await(cql"delete from users where userid=$userId".execute())
  }

  "cql...execute " should " be able to execute command " in {
    await(cql"delete from users where userid=12".execute())
  }

  def testImplicitFutureConvertToUnit(userId: Int): Future[Unit] = cql"delete from users where userid=$userId".execute()

  "cql...execute " should " be able to execute command and convert Future[Rows[_]] to Future[Unit[_]]" in {
    await(testImplicitFutureConvertToUnit(12))
  }

  case class U(userid: Int)
  "cql...execute " should " be able to execute with bind case class " in {
    await(cql"delete from users where userid=?".bind(U(12)).execute())
  }

  "cql...execute " should " be able to execute with parameters " in {
    await(cql"delete from users where userid=?".bindArgs(12).execute())
  }

  "cql...execute " should " be able to execute with 2 primitive parameters " in {
    await(cql"delete from users where userid in(?,?)".bindArgs(12, 13).execute())
  }

  "cql...execute " should " be able to select one row " in {
    val user = await(
      cql"select userId,name,created from users where userid=10".execute()
    ).unbind[Seq[User]].head

    assert(user.userId == 10)
    assert(user.name == "maga")
    assert(user.created == yesterday)
  }

  "cql...execute " should " be able to select one row with parameters " in {
    val user = await(
      cql"select userId,name,created from users where userid=?".bindArgs(11).execute()
    ).unbind[Seq[User]].head

    assert(user.userId == 11)
    assert(user.name == "alla")
    assert(user.created == yesterday)
  }

  "cql...execute " should " be able to select two rows with 2 plain parameters " in {
    val users = await(
      cql"select userId,name,created from users where userid in(?,?)".bindArgs(10, 11).execute()
    ).unbind[Seq[User]]

    assert(users.length == 2)
  }

  "cql...execute " should " be able to select rows " in {
    val users = await(
      cql"select userId,name,created from users where userid in (10,11)".execute()
    ).unbind[Seq[User]]
    assert(users.length == 2)
  }

  "cql...execute " should " be able to select 0 rows " in {
    val users = await(
      cql"select userId,name,created from users where userid in (12,13)".execute()
    ).unbind[Seq[User]]
    assert(users.length == 0)
  }

  "cql...execute " should " be able to execute with parameters in batch" in {
    val user1 = 12
    val user2 = 13
    val bs = new BatchStatement()
      .add(cql"delete from users where userid=$user1".boundStatement)
      .add(cql"delete from users where userid=$user2".boundStatement)
    session.execute(bs)
  }

  def deleteStmt(userId: Int) = cql"delete from users where userid=$userId"

  "cql...execute " should " be able to execute methods returning cql with parameters in batch" in {
    val bs = new BatchStatement()
      .add(deleteStmt(12).boundStatement)
      .add(deleteStmt(13).boundStatement)
    session.execute(bs)
  }

  "cql...oneApplied " should " return NotAppliedExists if condition is false and columns with original data" in {
    val userId = 10
    val a = await(cql"update users set name = 'magomed' where userid=$userId if name='not-maga'".oneApplied[UserName])
    assert(!a.isApplied)
    assert(a.isExists)
    assert(a.result.get.name == "maga")
  }

  "cql...oneApplied " should " return NotApplied if condition is false and original row doesn't exists" in {
    val userId = -1
    val a = await(cql"update users set name = 'magomed' where userid=$userId if name='not-maga'".oneApplied[UserName])
    assert(!a.isApplied)
    assert(!a.isExists)
    assert(a.result == None)
  }

  "cql...oneApplied " should " return Applied if condition is true" in {
    val userId = 10
    val a = await(cql"update users set name = 'magomed' where userid=$userId if name='maga'".oneApplied[UserName])
    assert(a.isApplied)
  }

  "batch...execute" should "execute one statement" in {
    val user = User(userId = newId(), name = "test", created = new Date())
    val a = await(Batch(cql"insert into users(userId, name, created) values(?,?,?)".bindArgs(user.userId, user.name, user.created)).execute())
    assert(a.wasApplied)
    assert(a.resultSet.all().size() == 0)

    val loadedUser = await(cql"SELECT * FROM users WHERE userId = ?".bindArgs(user.userId).one[User])
    assert(loadedUser.userId == user.userId)
    assert(loadedUser.name == user.name)
  }

  "batch...execute" should "execute several statements" in {
    val user = User(userId = newId(), name = "test", created = new Date())
    val user2 = User(userId = newId(), name = "test2", created = new Date())
    val a = await(Batch(
      cql"insert into users(userId, name, created) values(?,?,?)".bindArgs(user.userId, user.name, user.created),
      cql"insert into users(userId, name, created) values(?,?,?)".bindArgs(user2.userId, user2.name, user2.created)
    ).execute())
    assert(a.wasApplied)
    assert(a.resultSet.all().size() == 0)
  }

  "batch...oneApplied" should "execute insert with LWT for non-exists record" in {
    val user = User(userId = newId(), name = "test", created = new Date())
    val a = await(Batch(
      cql"insert into users(userId, name, created) values(?,?,?) IF NOT EXISTS".bindArgs(user.userId, user.name, user.created)
    ).oneApplied[User])

    assert(a.isApplied)
    assert(a.result.isEmpty)
  }

  "batch...oneApplied" should "execute insert with LWT for exists record" in {
    val user = User(userId = newId(), name = "test", created = new Date())
    createUser(user.userId, name = user.name, created = user.created)
    val a = await(Batch(
      cql"insert into users(userId, name, created) values(?,?,?) IF NOT EXISTS".bindArgs(user.userId, user.name, user.created)
    ).oneApplied[User])

    assert(!a.isApplied)
    assert(a.result.exists { u ⇒
      u.name == user.name &&
      u.userId == user.userId
    })
  }

  private def newId(): Int = Math.abs(Random.nextInt())



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
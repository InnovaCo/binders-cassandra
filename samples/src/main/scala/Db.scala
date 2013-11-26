import eu.inn.binders._
import eu.inn.binders.cassandra._

class Db(session: com.datastax.driver.core.Session) {
  // class for binding input/output parameters
  case class User(userId: Int, name: String)

  lazy val insertStatement = new Query(session,
    "insert into users(userid, name) values (?, ?)")

  def insertUser(user: User) = insertStatement.execute(user)

  lazy val selectAllStatement = new Query(session,
    "select * from users")

  def selectAllUsers = selectAllStatement.execute().unbindAll[User]

  lazy val selectUserStatement = new Query(session,
    "select * from users where userId = ?")

  def selectUser(userId: Int) = selectUserStatement.execute(userId).unbindOne[User]
}
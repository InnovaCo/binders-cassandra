import eu.inn.binders._
import eu.inn.binders.cassandra._
import eu.inn.binders.naming.PlainConverter
import scala.concurrent.{Future, ExecutionContext}

class Db(session: com.datastax.driver.core.Session) {

  import ExecutionContext.Implicits.global

  implicit val cache = new SessionQueryCache[PlainConverter](session)

  // class for binding input/output parameters
  case class User(userId: Int, name: String)

  def insertUser(user: User): Future[Unit] = cql"insert into users(userid, name) values (?, ?)".bind(user).execute()

  // returns Future[Iterator[User]]
  def selectAllUsers: Future[Iterator[User]] = cql"select * from users".all[User]

  // if no user is found will throw NoRowsSelectedException
  def selectUser(userId: Int) = cql"select * from users where userId = $userId".one[User]

  // if no user is found will return None, otherwise Some(User)
  def selectUserIfFound(userId: Int) = cql"select * from users where userId = $userId".oneOption[User]
}
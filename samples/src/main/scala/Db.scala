import eu.inn.binders._
import eu.inn.binders.cassandra._
import scala.concurrent.ExecutionContext

class Db(session: com.datastax.driver.core.Session) {

  import ExecutionContext.Implicits.global

  implicit val cache = new SessionQueryCache(session)

  // class for binding input/output parameters
  case class User(userId: Int, name: String)

  def insertUser(user: User) = CQL("insert into users(userid, name) values (?, ?)").execute(user)

  def selectAllUsers = CQL("select * from users")
    .execute()
    .map(_.unbindAll[User])

  def selectUser(userId: Int) = CQL("select * from users where userId = ?")
    .execute(userId)
    .map(_.unbindOne[User])
}
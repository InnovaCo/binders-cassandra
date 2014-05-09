import com.datastax.driver.core.{Session, Cluster}
import eu.inn.binders.naming.{Converter, PlainConverter}
import java.util.Date
import org.scalatest.{Suite, BeforeAndAfter}
import eu.inn.binders.cassandra._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

trait SessionFixture extends BeforeAndAfter {
  this: Suite =>
  var cluster: Cluster = null
  var session: Session = null
  implicit var sessionQueryCache: SessionQueryCache[PlainConverter] = null

  val yesterday = {
    import java.util._
    val cal = Calendar.getInstance()
    cal.setTime(new Date())
    cal.add(Calendar.DATE, -11)
    cal.getTime()
  }

  def createUser(id: Int, name: String, created: Date) = await(cql"insert into users(userId, name, created) values ($id,$name,$created)".executeStatement())

  before {
    cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    session = cluster.connect("binder_test")
    sessionQueryCache = new SessionQueryCache[PlainConverter](session)

    createUser(10,"maga",yesterday)
    createUser(11,"alla",yesterday)
  }

  after {
    if (session != null){
      session.close()
      session = null
    }
    if (cluster != null){
      cluster.close()
      cluster = null
    }
    sessionQueryCache = null
  }

  import scala.reflect.runtime.universe._
  def await[C <: Converter : TypeTag](r: Future[Rows[C]]): Rows[C] = Await.result(r, 20 seconds)
}

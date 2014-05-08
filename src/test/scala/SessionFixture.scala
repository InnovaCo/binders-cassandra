import com.datastax.driver.core.{Session, Cluster}
import eu.inn.binders.naming.PlainConverter
import org.scalatest.{Suite, BeforeAndAfter}
import eu.inn.binders.cassandra._
import scala.concurrent.Await
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

  before {
    cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    session = cluster.connect("binder_test")
    sessionQueryCache = new SessionQueryCache[PlainConverter](session)
    val insert = new AsyncQuery[PlainConverter](session, "insert into users(userId, name, created) values (10,'maga', ?)")
    Await.result(insert.execute(yesterday), 10 seconds)

    val insert2 = new AsyncQuery[PlainConverter](session, "insert into users(userId, name, created) values (11,'alla', ?)")
    Await.result(insert2.execute(yesterday), 10 seconds)
  }

  after {
    session.safeClose()
    session = null
    cluster.safeClose()
    cluster = null
    sessionQueryCache = null
  }
}

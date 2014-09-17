import com.datastax.driver.core.{Host, Session, Cluster}
import eu.inn.binders.naming.PlainConverter
import java.util.Date
import org.scalatest.{Suite, BeforeAndAfter}
import eu.inn.binders.cassandra._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

class StateListener extends Host.StateListener {

  val event = new Object
  @volatile var isHostAdded: Boolean = false

  def waitHostAdd() = {
    event.synchronized {
      while (!isHostAdded)
        event.wait()
    }
  }

  override def onAdd(p1: Host): Unit = {
    event.synchronized{
      isHostAdded = true
      event.notifyAll()
    }
  }

  override def onSuspected(p1: Host): Unit = {}
  override def onRemove(p1: Host): Unit = {}
  override def onUp(p1: Host): Unit = {}
  override def onDown(p1: Host): Unit = {}
}

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
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.getTime
  }

  def createUser(id: Int, name: String, created: Date) = {
      await(cql"insert into users(userId, name, created) values ($id,$name,$created)".execute())
  }

  before {
    cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val waiter = new StateListener
    cluster.register(waiter)
    session = cluster.connect("binder_test")
    waiter.waitHostAdd()

    //Thread.sleep(100)
    sessionQueryCache = new SessionQueryCache[PlainConverter](session)
    createUser(10, "maga", yesterday)
    createUser(11, "alla", yesterday)
  }

  after {
    if (session != null) {
      session.close()
      session = null
    }
    if (cluster != null) {
      cluster.close()
      cluster = null
    }
    sessionQueryCache = null
  }

  import scala.reflect.runtime.universe._

  def await[R: TypeTag](r: Future[R]): R = {
    Await.result(r, 20 seconds)
  }
}

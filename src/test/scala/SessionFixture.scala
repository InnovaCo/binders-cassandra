import java.util.Date

import com.datastax.driver.core.Session
import eu.inn.binders.cassandra._
import eu.inn.binders.naming.LowercaseConverter
import org.cassandraunit.CassandraCQLUnit
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.scalatest.{BeforeAndAfter, Suite}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Cassandra extends CassandraCQLUnit(new ClassPathCQLDataSet("bindersTest.cql","binders_test"), null, "127.0.0.1", 9142, 60000) {
  lazy val start = {
    before()
  }
}

trait SessionFixture extends BeforeAndAfter {
  this: Suite =>
  var session: Session = null
  implicit var sessionQueryCache: SessionQueryCache[LowercaseConverter] = null

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
    Cassandra.start
    session = Cassandra.session
    sessionQueryCache = new GuavaSessionQueryCache[LowercaseConverter](session)
    createUser(10, "maga", yesterday)
    createUser(11, "alla", yesterday)
  }

  after {
    //EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    sessionQueryCache = null
  }

  import scala.reflect.runtime.universe._

  def await[R: TypeTag](r: Future[R]): R = {
    Await.result(r, 20 seconds)
  }
}

import com.datastax.driver.core.Cluster
import scala.concurrent.Await
import scala.concurrent.duration._

object Sample {

  def main(args: Array[String]) {
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect("binder_test")

    val db = new Db(session)
    Await.result(db.insertUser(db.User(10, "John")), 10 seconds)

    val users = Await.result(db.selectAllUsers, 10 seconds)

    println(users.toList)

    cluster.close()
  }
}
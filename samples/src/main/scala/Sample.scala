import com.datastax.driver.core.Cluster

object Sample {

  def main(args: Array[String]) {
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect("capickling_tests")

    val db = new Db(session)
    db.insertUser(db.User(10, "John"))

    val users = db.selectAllUsers.toList

    println(users)

    cluster.shutdown()
  }
}
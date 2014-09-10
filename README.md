# binders-cassandra

This is a data binder library for Cassandra written in Scala. Please see more information at [binders library page](https://github.com/InnovaCo/binders)

Cassandra data using [DataStax Java Driver for Cassandra](https://github.com/datastax/java-driver).

## Example

Here is the sample of Scala class that is initialized with Cassandra session and allows you to do select/insert of some user data.

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

And this class could be used like this:

    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect("binder_test")

    val db = new Db(session)

    Await.result(db.insertUser(db.User(9, "John")), 10 seconds)

    val users = Await.result(db.selectAllUsers, 10 seconds)

    println(users.toList)

For the unit tests and sample application working local instance of Cassandra is required. Please see also sample project inside `samples/` folder and schema in `db/dbscript.cql`

## SBT

To use library, add this line to the build.sbt file:

    libraryDependencies += "eu.inn" %% "binders-cassandra" % "0.2.0"

## Requirements

Currently tested and works with:

* binders-core 0.2.2
* Cassandra 2.0.1 (corresponding driver with prepared statements)
* Scala 2.11.2, 2.10.4
* sbt 0.13
* guava from google 16.0.1

## License

Product licensed under BSD 3-clause as stated in file LICENSE

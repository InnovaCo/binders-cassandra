name := "binders-cassandra-sample"

version := "0.0-SNAPSHOT"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.file("Local Maven Repository",
  file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies += "eu.inn" %% "binders-cassandra" % "0.2.0"
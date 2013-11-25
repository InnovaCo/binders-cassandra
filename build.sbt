name := "binders-cassandra"

version := "0.0-SNAPSHOT"

organization := "eu.inn"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

resolvers += Resolver.file("Local Maven Repository", 
	file(Path.userHome.absolutePath+"/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.RC1" % "test"

libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % "test"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.0-beta1"

libraryDependencies += "eu.inn" %% "binders-core" % "0.0-SNAPSHOT"
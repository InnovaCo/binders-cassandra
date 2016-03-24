name := "binders-cassandra"

version := "0.9"

organization := "eu.inn"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.7", "2.10.5")

resolvers ++= Seq(
    Resolver.sonatypeRepo("public")
  )

libraryDependencies ++= Seq(
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.9",
  "com.google.guava" % "guava" % "19.0",
  "eu.inn" %% "binders-core" % "0.11.77",
  "org.slf4j" % "slf4j-api" % "1.7.7",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.cassandraunit" % "cassandra-unit" % "2.1.3.1" % "test",
  "junit" % "junit" % "4.12" % "test"
)

//libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.+"

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value
    // in Scala 2.10, quasiquotes are provided by macro paradise
    case Some((2, 10)) =>
      libraryDependencies.value ++ Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.1.0" cross CrossVersion.binary)
  }
}

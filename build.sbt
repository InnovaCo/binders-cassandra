name := "binders-cassandra"

version := "0.4.4"

organization := "eu.inn"

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.11.4", "2.10.4")

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.14" % "test"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.3"

libraryDependencies += "eu.inn" %% "binders-core" % "0.4.4"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.+"

libraryDependencies += "com.google.guava" % "guava" % "18.0"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value
    // in Scala 2.10, quasiquotes are provided by macro paradise
    case Some((2, 10)) =>
      libraryDependencies.value ++ Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.0.0" cross CrossVersion.binary)
  }
}

// Sonatype repositary publish options
publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := {
  _ => false
}

pomExtra := {
  <url>https://github.com/InnovaCo/binders-cassandra</url>
    <licenses>
      <license>
        <name>BSD-style</name>
        <url>http://opensource.org/licenses/BSD-3-Clause</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:InnovaCo/binders-cassandra.git</url>
      <connection>scm:git:git@github.com:InnovaCo/binders-cassandra.git</connection>
    </scm>
    <developers>
      <developer>
        <id>InnovaCo</id>
        <name>Innova Co S.a r.l</name>
        <url>https://github.com/InnovaCo</url>
      </developer>
      <developer>
        <id>maqdev</id>
        <name>Maga Abdurakhmanov</name>
        <url>https://github.com/maqdev</url>
      </developer>
    </developers>
}

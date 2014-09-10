name := "binders-cassandra"

version := "0.2.3"

organization := "eu.inn"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.11.2", "2.10.4")

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies <+= scalaVersion(scalatestDependency(_))

libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % "test"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.0"

libraryDependencies += "eu.inn" %% "binders-core" % "0.2.2"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.+"

libraryDependencies += "com.google.guava" % "guava" % "16.0.1"

def scalatestDependency(scalaVersion: String) = scalaVersion match {
  case s if s.startsWith("2.10.") => "org.scalatest" % "scalatest_2.10" % "2.2.0" % "test"
  case s if s.startsWith("2.11.") => "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"
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

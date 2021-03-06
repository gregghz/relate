name := "Relate"

organization := "com.lucidchart"

version := "1.4"

scalaVersion := "2.10.4"

exportJars := true

exportJars in Test := false

autoScalaLibrary := true

retrieveManaged := true

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "org.scalaz" %% "scalaz-effect" % "7.0.6",
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "mysql" % "mysql-connector-java" % "5.1.23"
)

pomExtra := (
  <url>https://github.com/lucidsoftware/relate</url>
  <licenses>
    <license>
      <name>Apache License</name>
      <url>http://www.apache.org/licenses/</url>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:lucidsoftware/relate.git</url>
    <connection>scm:git:git@github.com:lucidsoftware/relate.git</connection>
  </scm>
  <developers>
    <developer>
      <id>msiebert</id>
      <name>Mark Siebert</name>
    </developer>
    <developer>
      <id>gregghz</id>
      <name>Gregg Hernandez</name>
    </developer>
    <developer>
      <id>matthew-lucidchart</id>
      <name>Matthew Barlocker</name>
    </developer>
  </developers>
)

pomIncludeRepository := { _ => false }

useGpg := true

pgpReadOnly := false

publishMavenStyle := true

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD"))

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

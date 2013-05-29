name := "scala-force-layout"

version := "0.2.0"

scalaVersion := "2.10.1"

publishMavenStyle := true

libraryDependencies ++= Seq(
  "com.propensive" % "rapture-io" % "0.7.2"
)

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://github.com/rsimon/scala-force-layout</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:rsimon/scala-force-layout.git</url>
    <connection>scm:git:git@github.com:rsimon/scala-force-layout.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rsimon</id>
      <name>Rainer Simon</name>
      <url>http://rsimon.github.com</url>
    </developer>
  </developers>)

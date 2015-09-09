import play.Project._

name := """lobid-geo-enrichment"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.2", 
  "org.webjars" % "bootstrap" % "2.3.1",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.4.3",
  "org.elasticsearch" % "elasticsearch" % "1.3.6",
  "org.json" % "json" % "20141113")

playJavaSettings

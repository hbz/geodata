name := """geodata"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.4.3",
  "org.elasticsearch" % "elasticsearch" % "2.3.2" exclude ("io.netty", "netty"),
  "org.json" % "json" % "20141113")

name := """das-blog"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "org.elasticsearch" % "elasticsearch" % "2.2.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.2",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "net.java.dev.jna" % "jna" % "4.2.2" % "test"
)

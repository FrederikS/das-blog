name := """das-blog"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaStreamVersion = "2.4.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.3",
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
    "org.elasticsearch" % "elasticsearch" % "2.2.1",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.3",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.2",
    "commons-io" % "commons-io" % "2.4",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "net.java.dev.jna" % "jna" % "4.2.2" % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaStreamVersion,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2"
  )
}

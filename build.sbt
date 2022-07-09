scalaVersion := "2.13.8"
name := "kafka-streams"
organization := "rzk.scala"
version := "1.0"

libraryDependencies ++= kafkaDependencies ++ otherDependencies

val kafkaVersion = "3.2.0"
val circeVersion = "0.14.1"

val kafkaDependencies = Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

val otherDependencies = Seq(
    "com.typesafe" % "config" % "1.4.2",
    "org.slf4j" % "slf4j-log4j12" % "1.7.36",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
)

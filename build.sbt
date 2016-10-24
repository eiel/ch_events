name := "ch_events"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  var akkaVersion = "2.4.11"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe" % "config" % "1.3.1"
  )
}

cancelable in Global := true

assemblyJarName in assembly := "ch_events.jar"

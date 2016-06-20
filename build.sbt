name := "ch_events"

version := "1.0"

scalaVersion := "2.12.0"

libraryDependencies ++= {
  val akkaHttpVersion = "10.0.0"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe" % "config" % "1.3.1"
  )
}

cancelable in Global := true

assemblyJarName in assembly := "ch_events.jar"

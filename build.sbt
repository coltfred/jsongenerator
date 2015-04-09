name := "jsongenerator"

version := "0.1"

organization := "coltfred"

scalaVersion := "2.11.6"

libraryDependencies ++= List("org.scalaz" %% "scalaz-core" % "7.0.6",
  "org.scalaz" %% "scalaz-effect" % "7.0.6",
  "com.nicta" %% "rng" % "1.2.1",
  "io.argonaut" %% "argonaut" % "6.0.4",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

initialCommands in console := "import scalaz._, Scalaz._"

scalariformSettings


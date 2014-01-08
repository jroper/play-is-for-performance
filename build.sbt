import play.core.PlayVersion

name := "performance"

version := "1.0-SNAPSHOT"

play.Project.playScalaSettings

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-test" % PlayVersion.current,
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "org.webjars" %% "webjars-play" % "2.2.1",
  "org.webjars" % "prettify" % "4-Mar-2013",
  "org.webjars" % "chartjs" % "26962ce",
  "org.webjars" % "jquery" % "2.0.3-1",
  "org.webjars" % "requirejs" % "2.1.1"
)
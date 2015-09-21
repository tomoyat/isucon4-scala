lazy val root = (project in file(".")).
  settings(
    name := "isucon4",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq( jdbc , cache , ws )
  ).enablePlugins(PlayScala)

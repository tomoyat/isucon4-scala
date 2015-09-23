lazy val root = (project in file(".")).
    settings(
        name := "isucon4",
        version := "1.0",
        scalaVersion := "2.11.7",
        libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.27",
        libraryDependencies += "org.scalikejdbc" %% "scalikejdbc" % "2.2.8",
        libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-config" % "2.2.8",
        libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.4.1",
        libraryDependencies ++= Seq(jdbc, cache, ws)
    ).enablePlugins(PlayScala)

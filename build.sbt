val akkaHttpV  = "10.0.9"
val akkaV  = "2.4.16"
val scalaTest = "3.0.3"

lazy val root = (project in file(".")).
  settings(
    name := "bit-mixer",
    organizationName := "eremenko.ru",
    version := "0.1",
    scalaVersion := "2.12.4",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka"     %% "akka-http"              % akkaHttpV,
      "com.typesafe.akka"     %% "akka-http-spray-json"   % akkaHttpV,
      "com.typesafe.akka"     %% "akka-slf4j"             % akkaV,
      "com.typesafe.akka"     % "akka-http-testkit_2.12"  % akkaHttpV % "test",

      "joda-time"             % "joda-time"               % "2.9.2",
      "org.joda"              % "joda-convert"            % "1.8.1",
//      "com.github.tototoshi"  % "slick-joda-mapper_2.12"  % "2.3.0",

      "org.slf4j"             % "slf4j-api"               % "1.7.25",
      "org.slf4j"             % "slf4j-simple"            % "1.7.25",
      "org.scalatest"         % "scalatest_2.12"          % scalaTest % "test",
      "org.scalactic"         %% "scalactic"              % scalaTest,

      "org.typelevel"         %% "cats-core"              % "1.0.0-RC1"
//      "com.typesafe.scala-logging" %% "scala-logging"     % "3.5.0"
    )
  )

assemblyJarName in assembly := s"${name.value}-${git.gitHeadCommit.value.get}.jar"
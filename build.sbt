val Http4sVersion = "0.19.0-M1"
val LogbackVersion = "1.2.3"
val CirceVersion = "0.10.0-M2"

lazy val root = (project in file("."))
  .settings(
    organization := "uk.co.devproltd",
    name := "errorhandling",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    scalacOptions ++= Seq("-Ypartial-unification", "-feature"),
    scalafmtOnCompile in ThisBuild := true,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic"      % LogbackVersion,
      "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"     %% "http4s-circe"        % Http4sVersion,
      "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
      "com.olegpy"     %% "meow-mtl"            % "0.1.1"
    ) ++ Circe
  )

val Circe = Seq(
  "io.circe" %% "circe-core"    % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-parser"  % CirceVersion
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

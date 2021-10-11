name := "EnglishVocabulary"

lazy val reactJsV        = "17.0.2"
lazy val scalaJsReactV   = "2.0.0-RC3"
lazy val scalaCssV   = "0.8.0-RC1"
lazy val CirceVersion    = "0.14.1"
lazy val http4sVersion   = "0.23.5"
lazy val projectSettings = Seq(
  version      := "1.0",
  scalaVersion := "3.0.2")

lazy val common          = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("common"))
  .settings(projectSettings: _*)

lazy val `scalajs-client` = (project in file("scalajs-client"))
  .settings(projectSettings: _*)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
//      "org.scala-js"                      %%% "scalajs-dom"   % "1.2.0",
      "io.github.chronoscala"             %%% "chronoscala"   % "2.0.2",
      "com.github.japgolly.scalajs-react" %%% "core"          % scalaJsReactV,
      "com.github.japgolly.scalajs-react" %%% "extra"         % scalaJsReactV,
      "com.github.japgolly.scalacss"      %%% "ext-react"     % scalaCssV,
      "uz.scala"                          %%% "notification"  % "2.0.1",
      "io.circe"                          %%% "circe-core"    % CirceVersion,
      "io.circe"                          %%% "circe-parser"  % CirceVersion,
      "io.circe"                          %%% "circe-generic" % CirceVersion),
    webpackEmitSourceMaps           := false,
    webpackBundlingMode             := BundlingMode.Application,
    Compile / npmDependencies ++= Seq("react" -> reactJsV, "react-dom" -> reactJsV))
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(common.js)

lazy val `server` = project
  .in(file("server"))
  .dependsOn(common.jvm)
  .settings(projectSettings: _*)
  .settings(
    scalaJSProjects         := Seq(`scalajs-client`),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    pipelineStages          := Seq(digest, gzip),
    Compile / compile       := ((Compile / compile) dependsOn scalaJSPipeline).value)
  .settings(
    Global / onChangedBuildSource := IgnoreSourceChanges,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-server"       % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-scalatags"    % http4sVersion))
  .enablePlugins(WebScalaJSBundlerPlugin)

lazy val `english_vocabulary` = (project in file("."))
  .aggregate(`server`, `scalajs-client`)
Global / onLoad := (Global / onLoad).value.andThen(state => "project server" :: state)

import Deps._

name := "featury-api"

libraryDependencies ++= Seq(
  "org.http4s"       %% "http4s-dsl"           % http4sVersion,
  "org.http4s"       %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"       %% "http4s-blaze-client"  % http4sVersion,
  "org.http4s"       %% "http4s-circe"         % http4sVersion,
  "com.github.scopt" %% "scopt"                % "4.0.1",
  "ch.qos.logback"    % "logback-classic"      % logbackVersion,
  "io.prometheus"     % "simpleclient"         % prometheusVersion,
  "io.prometheus"     % "simpleclient_hotspot" % prometheusVersion,
  "io.prometheus"     % "simpleclient_common"  % prometheusVersion
)

scalacOptions += "-Ypartial-unification"

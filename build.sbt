import com.gu.riffraff.artifact.BuildInfo

name := "pluggable-content-rules"

organization := "com.gu"

description:= "A rules-engine service allowing non-dev Guardian staff to express what they want to see displayed on content"

version := "1.0"

scalaVersion := "3.2.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8"
)

val catsVersion = "2.9.0"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.2",
  "org.slf4j" % "log4j-over-slf4j" % "2.0.6", //  log4j-over-slf4j provides `org.apache.log4j.MDC`, which is dynamically loaded by the Lambda runtime
  "ch.qos.logback" % "logback-classic" % "1.4.5",
  "com.lihaoyi" %% "upickle" % "2.0.0",
  ("com.gu" %% "content-api-client-default" % "19.0.4").cross(CrossVersion.for3Use2_13),

  "com.google.api-client" % "google-api-client" % "2.2.0",
  "com.google.apis" % "google-api-services-sheets" % "v4-rev20230227-2.0.0",
  "com.google.auth" % "google-auth-library-credentials" % "1.16.1",
  "com.google.auth" % "google-auth-library-oauth2-http" % "1.16.1",

  "com.madgag" %% "scala-collection-plus" % "0.11",
  "com.google.http-client" % "google-http-client-gson" % "1.42.3",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "alleycats-core" % catsVersion

) ++ Seq("ssm", "url-connection-client").map(artifact => "software.amazon.awssdk" % artifact % "2.19.24")

Test / testOptions +=
  Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}")

enablePlugins(RiffRaffArtifact, BuildInfoPlugin)

assemblyJarName := s"${name.value}.jar"
riffRaffPackageType := assembly.value
riffRaffArtifactResources := Seq(
  (assembly/assemblyOutputPath).value -> s"${name.value}/${name.value}.jar",
  file("cdk/cdk.out/PluggableContentRules-PROD.template.json") -> s"cdk.out/PluggableContentRules-PROD.template.json",
  file("cdk/cdk.out/riff-raff.yaml") -> s"riff-raff.yaml"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

buildInfoPackage := "com.theguardian.content.rules"
buildInfoKeys := {
  lazy val buildInfo = BuildInfo(baseDirectory.value)
  Seq[BuildInfoKey](
    "buildNumber" -> buildInfo.buildIdentifier,
    "gitCommitId" -> buildInfo.revision,
    "buildTime" -> System.currentTimeMillis
  )
}

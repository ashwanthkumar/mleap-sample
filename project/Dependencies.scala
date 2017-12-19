import sbt._

object Dependencies {
  val scalaTest = "org.scalatest" %% "scalatest"   % "2.2.5"   % Test
  val mockito   = "org.mockito"   % "mockito-core" % "1.10.19" % Test

  val scalaJacksonModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.0"
  val akkaJackson        = "de.heikoseeberger"            %% "akka-http-jackson"    % "1.18.1"

  val akkaHttpVersion: String = "10.0.11"
  val akkaHttp                = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  val guice = "com.google.inject" % "guice"     % "4.1.0" exclude("com.google.guava", "guava")
  val joda  = "joda-time"         % "joda-time" % "2.9.3"

  val hocon     = "com.typesafe" % "config"     % "1.3.0"
  val commonsIO = "commons-io"   % "commons-io" % "2.5"

  val logbackVersion = "1.1.7"
  val logbackCore    = "ch.qos.logback" % "logback-core" % logbackVersion
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val logging        = Seq(logbackCore, logbackClassic)

  val mleapVersion = "0.8.1"
  val mleapRuntime = "ml.combust.mleap" %% "mleap-runtime" % mleapVersion
  val mleapCore = "ml.combust.mleap" %% "mleap-core" % mleapVersion
  val mleapSpark = "ml.combust.mleap" %% "mleap-spark" % mleapVersion
  val mleap = Seq(mleapCore, mleapSpark, mleapRuntime)

  val sparkML = "org.apache.spark" %% "spark-mllib" % "2.2.0"
  val netlibMac = "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()

  val metrics = "io.dropwizard.metrics" % "metrics-core" % "3.1.0"

  val utilsCore = "com.indix" %% "util-core" % "0.2.33"

  val commonDeps = Seq(joda)

  val testDeps = Seq(scalaTest, mockito)

  val hadoopVersion        = "2.6.5" // as used by spark 2.2.0
  val hadoopClient         = "org.apache.hadoop" % "hadoop-common" % hadoopVersion notTransitive ()
  val hadoopAws            = "org.apache.hadoop" % "hadoop-aws" % hadoopVersion
  val commonsConfiguration = "commons-configuration" % "commons-configuration" % "1.6"
  val hadoopAuth           = "org.apache.hadoop" % "hadoop-auth" % hadoopVersion
  val hadoopStack          = Seq(hadoopClient, commonsConfiguration, hadoopAuth, hadoopAws)

  val apiDependencies = commonDeps ++
    Seq(akkaHttp, guice, scalaJacksonModule, akkaJackson, commonsIO, utilsCore, sparkML, netlibMac) ++ logging ++ hadoopStack ++ mleap ++ testDeps

  def excludeLog4j(module: ModuleID) = {
    module
//      .exclude("log4j", "log4j")
      .exclude("org.slf4j", "slf4j-log4j12")
  }
}

sbtPlugin := true

organization := "com.github.plippe"
name := "sbt-awss3pages"

publishMavenStyle := false
bintrayRepository := "sbt"
bintrayOrganization in bintray := None

enablePlugins(GitVersioning)

scalaVersion := "2.12.4"
crossSbtVersions := Vector("0.13.16", "1.0.3")

libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-java-sdk-s3" % "1.11.234",
    Defaults.sbtPluginExtra(
        "com.typesafe.sbt" % "sbt-site" % "1.3.1",
        (sbtBinaryVersion in pluginCrossBuild).value,
        (scalaBinaryVersion in pluginCrossBuild).value
    ))

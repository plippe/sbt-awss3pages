sbtPlugin := true

organization := "com.github.plippe"
name := "sbt-awss3pages"

publishMavenStyle := false
bintrayRepository := "sbt"
bintrayOrganization in bintray := None

enablePlugins(GitVersioning)
git.useGitDescribe := true

scalaVersion := "2.12.7"
crossSbtVersions := Vector("0.13.17", "1.2.3")

libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-java-sdk-s3" % "1.11.419",
    Defaults.sbtPluginExtra(
        "com.typesafe.sbt" % "sbt-site" % "1.3.2",
        (sbtBinaryVersion in pluginCrossBuild).value,
        (scalaBinaryVersion in pluginCrossBuild).value
    ))

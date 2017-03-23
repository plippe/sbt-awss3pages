sbtPlugin := true

organization := "com.github.plippe"
name := "sbt-publish-doc"
version := "0.0.1"

publishMavenStyle := false
bintrayRepository := "sbt-plugin-releases"
bintrayOmitLicense := true
bintrayOrganization in bintray := None

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.105"

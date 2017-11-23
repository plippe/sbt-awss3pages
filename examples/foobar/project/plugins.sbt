// build root project
lazy val root = Project("plugins", file(".")) dependsOn(awsS3Pages)

// depends on the publishDoc project
lazy val awsS3Pages = RootProject(file("../../.."))

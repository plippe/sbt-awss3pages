// build root project
lazy val root = Project("plugins", file(".")) dependsOn(publishDoc)

// depends on the publishDoc project
lazy val publishDoc = file("..").getAbsoluteFile.toURI

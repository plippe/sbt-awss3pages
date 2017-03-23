resolvers += Resolver.url("plippe-sbt-plugin-releases", url("http://dl.bintray.com/plippe/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.plippe" % "sbt-publish-doc" % "0.0.1")

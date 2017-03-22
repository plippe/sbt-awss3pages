# sbt-publish-doc

Sbt plugin that publishes the documentation generated with `sbt doc`.


### Installing sbt-publish-doc

As sbt-publish-doc is a plugin for sbt, it is installed like any other sbt plugin, that is by mere configuration.
For details about using sbt plugins, please refer to [sbt Getting Started Guide / Using Plugins](http://www.scala-sbt.org/release/docs/Getting-Started/Using-Plugins.html).

To add sbt-publish-doc to your build, two updates are required.

```sbt
// in project/plugins.sbt
resolvers += Resolver.url(
    "plippe-sbt-plugin-releases",
    url("http://dl.bintray.com/plippe/sbt-plugin-releases")
    )(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.plippe" % "sbt-publish-doc" % "0.1.0")
```

```sbt
// in build.sbt
enablePlugins(PublishDoc)
```


### Using sbt-publish-doc

If you updated your build files like described above, you should have access to a new sbt task.


#### sbt publishDoc:amazonS3
`publishDoc:amazonS3` will publish the documentation to [Amazon S3](http://docs.aws.amazon.com/AmazonS3/latest/dev/Welcome.html).

There are two big advantages to this:
 - [S3 can host a static website](http://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteHosting.html)
 - [S3 access can be restricted to specific IP Addresses](http://docs.aws.amazon.com/AmazonS3/latest/dev/example-bucket-policies.html#example-bucket-policies-use-case-3)

`publishDocAmazonS3Uri` setting is required. It must be set to the documentation's destination on Amazon S3.

```sbt
// in build.sbt
publishDocAmazonS3Uri := "s3://[BUCKET]/[KEY]"

// OR
publishDocAmazonS3Uri := s"s3://[BUCKET]/[KEY]/${organization.value}/${name.value}/${version.value}"
```

`publishDocAmazonS3Client` setting, by default, is set to [`AmazonS3ClientBuilder.defaultClient()`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3ClientBuilder.html#defaultClient--),
but it can be overwritten in the `build.sbt` file.

```sbt
// in build.sbt
publishDocAmazonS3Client := AmazonS3ClientBuilder.standard()
    .withRegion(Regions.US_EAST_1)
    .withCredentials(new ProfileCredentialsProvider("my-profile"))
    ...
    .build()
```

Once your build files are up to date, run `sbt publishDoc:amazonS3` to publish the documentation. It will
be accessible at `[BUCKET].s3-website-[REGION].amazonaws.com/[KEY]/index.html`.

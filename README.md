# sbt-awss3pages

Sbt plugin similar to [sbt-ghpages][sbt-ghpages], it pushes your [sbt-site][sbt-site] to [S3][s3]. The main advantage
over using [github pages][github-page] is the ability to [restrict access][s3-access].


### Installing sbt-awss3pages

To add `sbt-awss3pages`, just update your `project/plugins.sbt` file and enable it in your `build.sbt`.

```sbt
// in project/plugins.sbt
resolvers += Resolver.url("plippe-sbt-awss3pages", url("http://dl.bintray.com/plippe/sbt"))(Resolver.ivyStylePatterns)
addSbtPlugin("com.github.plippe" % "sbt-awss3pages" % "XXX")
```

```sbt
// in build.sbt
enablePlugins(AwsS3PagesPlugin)
```


### Using sbt-awss3pages

`sbt-awss3pages` has only two settings to configure:
  - `awsS3PagesClient`, the amazon S3 client to push the site, defaults to [`AmazonS3ClientBuilder.defaultClient()`][s3-client-default]
  - `awsS3PagesUri`, URI to the S3 folder which will contain the site

These settings can be configured in your `build.sbt`:
```sbt
// in build.sbt

awsS3PagesUri := new com.amazonaws.services.s3.AmazonS3URI("s3://bucket/key/${organization.value}/${name.value}/${version.value}")
awsS3PagesClient := com.amazonaws.services.s3.AmazonS3ClientBuilder
    .standard()
    .withRegion(com.amazonaws.regions.Regions.US_EAST_1)
    .withCredentials(new com.amazonaws.auth.profile.ProfileCredentialsProvider("my-profile"))
    ...
    .build()
```

Once configured, run `sbt awsS3PagesPushSite` to push your files to S3.


### Using sbt-awss3pages to publish scala docs

[sbt-site][sbt-site] can easily be used to [include Scaladoc with your site][sbt-site-scaladoc]. Pushing those files
to S3 only requires the addition of a plugin. Furthermore, the path of the scaladoc can be configured.

```sbt
enablePlugins(SiteScaladocPlugin)

siteSubdirName in SiteScaladoc := "" // move scaladoc at the root of the site
```

[sbt-ghpages]: https://github.com/sbt/sbt-ghpages
[sbt-site]: https://github.com/sbt/sbt-site
[sbt-site-scaladoc]: http://www.scala-sbt.org/sbt-site/api-documentation.html#scaladoc
[s3]: https://aws.amazon.com/s3/
[s3-access]: http://docs.aws.amazon.com/AmazonS3/latest/dev/s3-access-control.html
[s3-client-default]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3ClientBuilder.html#defaultClient
[github-page]: https://pages.github.com/

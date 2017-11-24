# sbt-awss3pages

[ ![Download][download_img] ][download_link]

Sbt plugin similar to [sbt-ghpages][sbt-ghpages], it pushes your [sbt-site][sbt-site] to [Amazon S3][s3]. The main
advantage over using [github pages][github-page] is the ability to [restrict access][s3-access].


### AWS S3 website hosting

To host a website on S3, just follow Amazon's documentation on [hosting a static website on Amazon S3][s3-website].


### Installing sbt-awss3pages

Add the plugin to `project/plugins.sbt` file.

```sbt
// in project/plugins.sbt
resolvers += Resolver.url("plippe-sbt", url("http://dl.bintray.com/plippe/sbt"))(Resolver.ivyStylePatterns)
addSbtPlugin("com.github.plippe" % "sbt-awss3pages" % "XXX")
```

Enable the plugin in your `build.sbt` file.

```sbt
// in build.sbt
enablePlugins(AwsS3PagesPlugin)
```


### Using sbt-awss3pages

The only requirement is setting the `awsS3PagesUri` setting in your `build.sbt`. This `AmazonS3URI` object represents
the S3 folder which will contain the site.

```sbt
// in build.sbt
awsS3PagesUri := new com.amazonaws.services.s3.AmazonS3URI(
  "s3://bucket/key/${organization.value}/${name.value}/${version.value}"
)
```

The `awsS3PagesClient` setting defaults to [`AmazonS3ClientBuilder.defaultClient()`][s3-client-default]. It can be
overridden with your own Amazon client.

```sbt
// in build.sbt
awsS3PagesClient := com.amazonaws.services.s3.AmazonS3ClientBuilder
    .standard()
    .withRegion(com.amazonaws.regions.Regions.US_EAST_1)
    .withCredentials(new com.amazonaws.auth.profile.ProfileCredentialsProvider("my-profile"))
    ...
    .build()
```

Once configured, run `sbt awsS3PagesPushSite` to push your files to S3.


#### Pushing scaladoc

[sbt-site][sbt-site] can easily be used to [include Scaladoc with your site][sbt-site-scaladoc].

```sbt
enablePlugins(SiteScaladocPlugin)
siteSubdirName in SiteScaladoc := "" // move scaladoc at the root of the site
```


[download_img]: https://api.bintray.com/packages/plippe/sbt/sbt-awss3pages/images/download.svg
[download_link]: https://bintray.com/plippe/sbt/sbt-awss3pages/_latestVersion

[sbt-site]: https://github.com/sbt/sbt-site
[sbt-site-scaladoc]: http://www.scala-sbt.org/sbt-site/api-documentation.html#scaladoc
[sbt-ghpages]: https://github.com/sbt/sbt-ghpages

[github-page]: https://pages.github.com/

[s3]: https://aws.amazon.com/s3/
[s3-website]: http://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteHosting.html
[s3-access]: http://docs.aws.amazon.com/AmazonS3/latest/dev/s3-access-control.html
[s3-client-default]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3ClientBuilder.html#defaultClient

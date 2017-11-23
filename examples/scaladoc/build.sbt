enablePlugins(AwsS3PagesPlugin)
enablePlugins(SiteScaladocPlugin)

awsS3PagesUri := new com.amazonaws.services.s3.AmazonS3URI("s3://bucket/key/${organization.value}/${name.value}/${version.value}")
siteSubdirName in SiteScaladoc := ""

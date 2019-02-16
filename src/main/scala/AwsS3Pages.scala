import sbt._, Keys._

import com.typesafe.sbt.site.{ SitePlugin, SiteScaladocPlugin }
import com.typesafe.sbt.site.SitePlugin.autoImport.siteSubdirName
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder, AmazonS3URI }
import com.amazonaws.services.s3.model.{DeleteObjectsRequest, ListObjectsV2Request}

import scala.collection.JavaConverters._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

trait AwsS3PagesKeys {

  lazy val awsS3PagesClient = settingKey[AmazonS3]("Amazon S3 client to push the site")
  lazy val awsS3PagesUri = settingKey[AmazonS3URI]("Uri to the S3 folder which will contain the site")

  lazy val awsS3PagesPushSite = taskKey[Unit]("Pushes a generated site into aws S3")

}

object AwsS3PagesPlugin extends AutoPlugin {

  override val  requires: Plugins = SitePlugin && SiteScaladocPlugin
  override lazy val  projectSettings: Seq[Setting[_]] = awsS3PagesProjectSettings

  object autoImport extends AwsS3PagesKeys
  import autoImport._

  def awsS3PagesProjectSettings: Seq[Setting[_]] = Seq(
    awsS3PagesClient := AmazonS3ClientBuilder.defaultClient(),
    awsS3PagesPushSite := pushSite.value,

    autoAPIMappings := true,
    apiURL := {
      val scaladocDir = (siteSubdirName in SiteScaladocPlugin.autoImport.SiteScaladoc).value

      val awsRegion = awsS3PagesClient.value.getRegionName
      val awsS3Bucket = awsS3PagesUri.value.getBucket
      val awsS3Key = s"${awsS3PagesUri.value.getKey}/${scaladocDir}"

      Some(url(s"http://${awsS3Bucket}.s3-website-${awsRegion}.amazonaws.com/${awsS3Key}"))
    }
  )

  def pushSite = Def.task {
    implicit val log = streams.value.log

    log.info("Making site")
    val site = (mappings in SitePlugin.autoImport.makeSite).value

    log.info(s"Reading local files")
    val localFiles = Local.files(site, awsS3PagesUri.value)
    log.info(s"Reading done, ${localFiles.size} local files found")

    log.info(s"Reading AWS S3 files")
    val s3Files = AwsS3.files(awsS3PagesClient.value, awsS3PagesUri.value)
    log.info(s"Reading done, ${s3Files.size} AWS S3 files found")

    log.info(s"Syncing local, and AWS S3 files")
    AwsS3.sync(awsS3PagesClient.value, localFiles, s3Files)
    log.info(s"Syncing done")
  }

}

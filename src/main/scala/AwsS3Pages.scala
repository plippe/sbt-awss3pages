import sbt._, Keys._

import com.typesafe.sbt.site.{ SitePlugin, SiteScaladocPlugin }
import com.typesafe.sbt.site.SitePlugin.autoImport.siteSubdirName
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder, AmazonS3URI }

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

      val awsRegion = awsS3PagesClient.value.getRegion
      val awsS3Bucket = awsS3PagesUri.value.getBucket
      val awsS3Key = s"${awsS3PagesUri.value.getKey}/${scaladocDir}"

      Some(url(s"http://${awsS3Bucket}.s3-website-${awsRegion}.amazonaws.com/${awsS3Key}"))
    }
  )

  def pushSite = Def.task {
    streams.value.log.info(s"Pushing site to ${awsS3PagesUri.value}")

    val pushRequests = (mappings in SitePlugin.autoImport.makeSite)
      .value
      .filter { case (file, target) => file.isFile() }
      .map { case (file, target) =>
        val targetS3Uri = new AmazonS3URI(s"${awsS3PagesUri.value}${target}", true)

        streams.value.log.info(s"Pushing ${target} to ${targetS3Uri}")
        Future { awsS3PagesClient.value.putObject(targetS3Uri.getBucket, targetS3Uri.getKey, file) }
      }

    Await.result(Future.sequence(pushRequests), Duration.Inf)

    streams.value.log.info(s"Pushing done")
  }

}

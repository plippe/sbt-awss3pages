import sbt._, Keys._

import com.typesafe.sbt.site.SitePlugin
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder, AmazonS3URI }

trait AwsS3PagesKeys {
  lazy val awsS3PagesClient = settingKey[AmazonS3]("Amazon S3 client to push the site")
  lazy val awsS3PagesUri = settingKey[AmazonS3URI]("Uri to the S3 folder which will contain the site")

  lazy val awsS3PagesPushSite = taskKey[Unit]("Pushes a generated site into aws S3")

  lazy val awsS3PagesPrivateMappings = mappings in SitePlugin.autoImport.makeSite
}

object AwsS3PagesPlugin extends AutoPlugin {
  override val  requires: Plugins = SitePlugin
  override lazy val  projectSettings: Seq[Setting[_]] = awsS3PagesProjectSettings

  object autoImport extends AwsS3PagesKeys
  import autoImport._

  def awsS3PagesProjectSettings: Seq[Setting[_]] = Seq(
    awsS3PagesClient := AmazonS3ClientBuilder.defaultClient(),
    awsS3PagesPushSite := pushSite.value
  )

  def pushSite = Def.task {
    streams.value.log.info(s"Pushing site to ${awsS3PagesUri.value}")
    awsS3PagesPrivateMappings.value
      .filter { case (file, target) => file.isFile() }
      .foreach { case (file, target) =>
        val targetS3Uri = new AmazonS3URI(s"${awsS3PagesUri.value}${target}", true)

        streams.value.log.info(s"Pushing ${file} to ${targetS3Uri}")
        awsS3PagesClient.value.putObject(targetS3Uri.getBucket, targetS3Uri.getKey, file)
      }

    streams.value.log.info(s"Pushing done")
  }

}

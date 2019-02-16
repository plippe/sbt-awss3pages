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
    streams.value.log.info(s"Pushing site to ${awsS3PagesUri.value}")

    val localFiles: Map[AmazonS3URI, (Long, File)] = (mappings in SitePlugin.autoImport.makeSite)
      .value
      .filter { case (file, target) => file.isFile() }
      .map { case (file, target) =>
        val uri = new AmazonS3URI(s"${awsS3PagesUri.value}${target}", true)
        uri -> (file.length, file)
      }
      .toMap

    val s3Files: Map[AmazonS3URI, Long] = {
      @annotation.tailrec
      def rec(request: ListObjectsV2Request, accumulator: Map[AmazonS3URI, Long] = Map.empty): Map[AmazonS3URI, Long] = {
        val result = awsS3PagesClient.value.listObjectsV2(request)
        val newAccumulator = accumulator ++ result.getObjectSummaries()
          .asScala
          .map { summary =>
            val uri = new AmazonS3URI(s"s3://${summary.getBucketName()}/${summary.getKey()}")
            uri -> summary.getSize()
          }

        Option(result.getNextContinuationToken) match {
          case None => newAccumulator
          case Some(token) =>
            val newRequest = request.withContinuationToken(token)
            rec(newRequest, newAccumulator)
        }
      }

      val request = new ListObjectsV2Request()
        .withBucketName(awsS3PagesUri.value.getBucket)
        .withPrefix(awsS3PagesUri.value.getKey)

      rec(request)
    }

    val (toDelete, toPut) = (localFiles.keys ++ s3Files.keys)
      .map { uri =>
        (uri, localFiles.get(uri), s3Files.get(uri))
      }
      .collect {
        case (uri, Some((localSize, file)), Some(s3Size)) if localSize != s3Size => Right((uri, file))
        case (uri, Some((_, file)), None) => Right((uri, file))
        case (uri, None, Some(_)) => Left(uri)
      }
      .foldLeft((List.empty[AmazonS3URI], List.empty[(AmazonS3URI, File)])) {
        case ((ls, rs), Left(l)) => (ls :+ l, rs)
        case ((ls, rs), Right(r)) => (ls, rs :+ r)
      }

    streams.value.log.info(s"Deleting removed files, ${toDelete.length} files in total")
    toDelete
      .grouped(100)
      .foreach { uris =>
        val request = new DeleteObjectsRequest(awsS3PagesUri.value.getBucket)
          .withKeys(uris.map(_.getKey): _*)

        awsS3PagesClient.value.deleteObjects(request)
      }

    streams.value.log.info(s"Pushing new, and updated files, ${toPut.length} files in total")
    toPut.foreach { case (uri, file) =>
        awsS3PagesClient.value.putObject(uri.getBucket, uri.getKey, file)
      }

    streams.value.log.info(s"Pushing done")
  }

}

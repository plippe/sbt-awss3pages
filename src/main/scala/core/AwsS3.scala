import com.amazonaws.services.s3.{ AmazonS3, AmazonS3URI }
import com.amazonaws.services.s3.model.{ListObjectsV2Request, S3ObjectSummary}
import java.io.File
import scala.collection.JavaConverters._
import sbt.Logger

object AwsS3 {

  def files(amazonS3: AmazonS3, uri: AmazonS3URI)(implicit log: Logger): Map[AmazonS3URI, S3ObjectSummary] = {
    @annotation.tailrec
    def rec(request: ListObjectsV2Request, accumulator: Map[AmazonS3URI, S3ObjectSummary] = Map.empty): Map[AmazonS3URI, S3ObjectSummary] = {
      log.debug(s"Reading in progress, ${accumulator.size} AWS S3 files found at the moment")

      val result = amazonS3.listObjectsV2(request)
      val newAccumulator = accumulator ++ result.getObjectSummaries()
        .asScala
        .map { summary =>
          val uri = new AmazonS3URI(s"s3://${summary.getBucketName()}/${summary.getKey()}")
          uri -> summary
        }

      Option(result.getNextContinuationToken) match {
        case None => newAccumulator
        case Some(token) =>
          val newRequest = request.withContinuationToken(token)
          rec(newRequest, newAccumulator)
      }
    }

    val request = new ListObjectsV2Request()
      .withBucketName(uri.getBucket)
      .withPrefix(uri.getKey)

    rec(request)
  }

  def sync(amazonS3: AmazonS3, local: Map[AmazonS3URI, File], remote: Map[AmazonS3URI, S3ObjectSummary])(implicit log: Logger): Unit =
    (local.keys ++ remote.keys)
      .map { uri => sync(amazonS3, uri, local.get(uri), remote.get(uri)) }
      .foreach { sync => sync.unsafeRun }

  def sync(amazonS3: AmazonS3, uri: AmazonS3URI, local: Option[File], remote: Option[S3ObjectSummary])(implicit log: Logger): AwsS3Sync = {
    (local, remote) match {
      case (Some(file), Some(summary)) if file.length != summary.getSize => AwsS3Sync.Upload(amazonS3, uri, file)
      case (Some(file), None) => AwsS3Sync.Upload(amazonS3, uri, file)
      case (None, Some(_)) => AwsS3Sync.Delete(amazonS3, uri)
      case _ => AwsS3Sync.Skip(uri)
    }
  }
}

class AwsS3(amazonS3: AmazonS3)(implicit log: Logger) {
  def files(uri: AmazonS3URI) = AwsS3.files(amazonS3, uri)
  def sync(local: Map[AmazonS3URI, File], remote: Map[AmazonS3URI, S3ObjectSummary]) = AwsS3.sync(amazonS3, local, remote)
}

import com.amazonaws.services.s3.{ AmazonS3, AmazonS3URI }
import java.io.File
import sbt.Logger

sealed trait AwsS3Sync { def unsafeRun: Unit }
object AwsS3Sync {
  case class Skip(uri: AmazonS3URI)(implicit log: Logger) extends AwsS3Sync {
    def unsafeRun: Unit =
      log.debug(s"Skipping ${uri}")
  }

  case class Upload(amazonS3: AmazonS3, uri: AmazonS3URI, file: File)(implicit log: Logger) extends AwsS3Sync {
    def unsafeRun: Unit = {
      log.info(s"Uploading ${file} to ${uri}")
      amazonS3.putObject(uri.getBucket, uri.getKey, file)
    }
  }

  case class Delete(amazonS3: AmazonS3, uri: AmazonS3URI)(implicit log: Logger) extends AwsS3Sync {
    def unsafeRun: Unit = {
      log.info(s"Deleting ${uri}")
      amazonS3.deleteObject(uri.getBucket, uri.getKey)
    }
  }
}

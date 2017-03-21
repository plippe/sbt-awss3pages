import sbt._, Keys._

import scala.annotation.tailrec

import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder, AmazonS3URI }

object PublishDoc extends AutoPlugin {

  object autoImport {
    lazy val PublishDocSettings = config("publish-doc") describedAs("Publish documentation")

    lazy val amazonS3 = TaskKey[Unit]("amazon-s3", "Publish documentation to Amazon S3") in PublishDocSettings
    lazy val amazonS3Client = SettingKey[AmazonS3]("amazon-s3-client", "Client to publish documentation") in PublishDocSettings
    lazy val amazonS3Uri = SettingKey[String]("amazon-s3-uri", "Uri to the s3 folder which will contain the documentation") in PublishDocSettings
  }

  import autoImport._

  def filesIn(file: File): List[File] = {
    @tailrec
    def filesIn(files: List[File], acc: List[File]): List[File] = files match {
      case Nil => acc
      case head :: tail if head.isFile => filesIn(tail, head :: acc)
      case head :: tail if head.isDirectory => filesIn(head.listFiles.toList ::: tail, acc)
    }

    filesIn(List(file), Nil)
  }

  lazy val amazonS3Settings = Seq(
    amazonS3Client := AmazonS3ClientBuilder.defaultClient(),
    amazonS3 := {
      val s3Uri = new AmazonS3URI(amazonS3Uri.value)

      val crossTargetFolder = (crossTarget in Compile).value.getAbsolutePath
      val documentationFolder = (doc in Compile).value

      streams.value.log.info(s"Publishing Scala API documentation $documentationFolder to ${amazonS3Uri.value}")

      filesIn(documentationFolder).foreach { documentationFile =>
        streams.value.log.info(documentationFile.getAbsolutePath)

        val s3Key = documentationFile.getAbsolutePath.replace(crossTargetFolder, s3Uri.getKey)
        amazonS3Client.value.putObject(s3Uri.getBucket, s3Key, documentationFile)

      }
    }
  )

  override lazy val projectSettings = inConfig(PublishDocSettings)(amazonS3Settings)
}

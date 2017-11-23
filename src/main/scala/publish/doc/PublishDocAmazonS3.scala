package com.github.plippe.publish.doc

import sbt._, Keys._

import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder, AmazonS3URI }

import com.github.plippe.Files

object PublishDocAmazonS3Keys {
  lazy val publishDocAmazonS3Client = settingKey[AmazonS3]("Client to publish documentation")
  lazy val publishDocAmazonS3Uri = settingKey[String]("Uri to the s3 folder which will contain the documentation")

  lazy val publishDocAmazonS3 = TaskKey[Unit]("amazonS3", "Publish documentation to Amazon S3") in PublishDocKeys.publishDocNamespace
}

object PublishDocAmazonS3 extends AutoPlugin {

  val autoImport = PublishDocAmazonS3Keys
  override lazy val projectSettings = Seq(
    PublishDocAmazonS3Keys.publishDocAmazonS3Client := AmazonS3ClientBuilder.defaultClient(),
    PublishDocAmazonS3Keys.publishDocAmazonS3 := publishDocAmazonS3.value
  )

  lazy val publishDocAmazonS3 = Def.task {
    val amazonS3Uri = new AmazonS3URI(PublishDocAmazonS3Keys.publishDocAmazonS3Uri.value)
    val amazonS3Client = PublishDocAmazonS3Keys.publishDocAmazonS3Client.value

    val crossTargetFolder = (crossTarget in Compile).value.getAbsolutePath
    val docFolder = (doc in Compile).value

    streams.value.log.info(s"Publishing Scala API documentation $docFolder to $amazonS3Uri")

    Files.in(docFolder).foreach { docFile =>
      val s3Key: String = Files.popApiDir(docFile.getAbsolutePath.replace(crossTargetFolder, amazonS3Uri.getKey))

      streams.value.log.info(s"uploading ${docFile.getAbsolutePath} to $s3Key")

      amazonS3Client.putObject(amazonS3Uri.getBucket, s3Key, docFile)
    }
  }

}

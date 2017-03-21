package com.github.plippe.publish.doc

import sbt._, Keys._

object PublishDocKeys {
  lazy val publishDocNamespace = config("publishDoc") describedAs("Publish documentation")
  lazy val publishDocDefault = TaskKey[Unit]("publishDoc", "Publish documentation")
}

object PublishDoc extends AutoPlugin {
  override def requires = PublishDocAmazonS3

  val autoImport = PublishDocKeys
  override lazy val projectSettings = Seq(
    PublishDocKeys.publishDocDefault := PublishDocAmazonS3.publishDocAmazonS3.value
  )
}

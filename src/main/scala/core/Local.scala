
import com.amazonaws.services.s3.AmazonS3URI
import java.io.File

object Local {

  def files(site: Seq[(File, String)], uri: AmazonS3URI): Map[AmazonS3URI, File] =
    site
      .filter { case (file, target) => file.isFile() }
      .map { case (file, target) =>
        new AmazonS3URI(s"${uri}${target}", true) -> file
      }
      .toMap

}

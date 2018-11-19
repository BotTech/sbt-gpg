package nz.co.bottech.sbt.gpg

import java.io.File
import java.nio.file.Files

sealed trait GpgArgument {

  def prepare(): Seq[String]
}

final case class GpgFlag(flag: String) extends GpgArgument {

  override def prepare(): Seq[String] = Seq(flag)
}

object GpgFlag {

  val armor = GpgFlag("--armor")
  val batch = GpgFlag("--batch")
  val fingerprint = GpgFlag("--fingerprint")
  val verbose = GpgFlag("--verbose")
  val withColon = GpgFlag("--with-colons")
  val withKeyGrip = GpgFlag("--with-keygrip")
  val withSecret = GpgFlag("--with-secret")
}

final case class GpgOption(option: String, value: () => String) extends GpgArgument {

  override def prepare(): Seq[String] = Seq(option, value())
}

object GpgOption {

  val homeDir = DirectoryOption("--homedir", create = true)
  val output = FileOption("--output", delete = true)
  val localUser = ToStringOption("--local-user")
  val passphraseFile = FileOption("--passphrase-file", delete = false)
  val pinentryMode = ToStringOption("--pinentry-mode")
  val statusFD = ToStringOption("--status-fd")
}

final case class DirectoryOption(option: String, create: Boolean) extends (File => GpgOption) {

  override def apply(directory: File): GpgOption = {
    GpgOption(option, prepare(directory))
  }

  private def prepare(directory: File): () => String = () => {
    if (create) {
      val _ = Files.createDirectories(directory.toPath)
    }
    directory.toString
  }
}

final case class FileOption(option: String, delete: Boolean) extends (File => GpgOption) {

  override def apply(file: File): GpgOption = GpgOption(option, prepare(file))

  private def prepare(file: File): () => String = () => {
    if (delete) {//} && file.canWrite) {
      println(s"***** File '${file.getPath}' exists ${file.exists()} canWrite ${file.canWrite}")
      val _ = Files.deleteIfExists(file.toPath)
    }
    file.getPath
  }
}

final case class ToStringOption(option: String) extends (Any => GpgOption) {

  override def apply(value: Any): GpgOption = GpgOption(option, () => value.toString)
}

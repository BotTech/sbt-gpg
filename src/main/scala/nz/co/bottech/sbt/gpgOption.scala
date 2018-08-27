package nz.co.bottech.sbt

import java.io.File

trait GpgOption[A] {

  def option: String

  def valueToOption(value: A): String

  def apply(value: A): GpgOptionValue = GpgOptionValue(option, valueToOption(value))
}

object GpgOption {

  val homeDir: GpgOption[File] = FileOption("--homedir")
  val statusFD: GpgOption[Int] = ToStringOption[Int]("--status-fd")
}

final case class FileOption(option: String) extends GpgOption[File] {

  override def valueToOption(value: File): String = value.getAbsolutePath
}

final case class ToStringOption[A](option: String) extends GpgOption[A] {

  override def valueToOption(value: A): String = value.toString
}

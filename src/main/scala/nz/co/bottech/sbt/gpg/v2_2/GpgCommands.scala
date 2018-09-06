package nz.co.bottech.sbt.gpg.v2_2

import java.io.File

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  final val GpgCommandProperty = "gpg.command.v2_2"
  final val GpgCommand = "gpg"
  final val GpgVersionRegex = "gpg.* 2.2.*"
  final val FullGenerateKeyCommand = "--full-generate-key"
  final val VersionCommand = "--version"

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    val gpg = System.getProperty(GpgCommandProperty, GpgCommand)
    executeVersionCommand(gpg, VersionCommand, GpgVersionRegex, GpgVersion2Dot2, log)
  }

  override def commonArguments(homeDirectory: File, statusFileDescriptor: Int): Seq[GpgArgument] = {
    Seq(
      GpgOption.homeDir(homeDirectory),
      GpgFlag.batch,
      GpgFlag.withColon,
      GpgOption.statusFD(statusFileDescriptor)
    )
  }

  override def generateKey(gpg: String, options: Seq[String], log: Logger): Unit = {
    // TODO: Add the remaining options.
    execute(gpg, options, FullGenerateKeyCommand, log)
  }
}

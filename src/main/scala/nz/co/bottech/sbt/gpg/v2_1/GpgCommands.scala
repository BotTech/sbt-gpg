package nz.co.bottech.sbt.gpg.v2_1

import java.io.File

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  final val GpgCommandProperty = "gpg.command.v2_1"
  final val GpgCommand = "gpg2"
  final val GpgVersionRegex = "gpg.* 2.1.*"
  final val VersionCommand = "--version"

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    val gpg = System.getProperty(GpgCommandProperty, GpgCommand)
    executeVersionCommand(gpg, VersionCommand, GpgVersionRegex, GpgVersion2Dot1, log)
  }

  override def commonArguments(homeDirectory: File, statusFileDescriptor: Int): Seq[GpgArgument] = {
    // TODO: Implement this.
    Seq.empty
  }

  override def generateKey(gpg: String, options: Seq[String], log: Logger): Unit = {
    // TODO: Implement this.
  }
}

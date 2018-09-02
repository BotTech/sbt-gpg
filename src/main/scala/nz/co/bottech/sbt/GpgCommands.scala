package nz.co.bottech.sbt

import java.io.File

import scala.sys.process._

object GpgCommands {

  final val FullGenerateKeyCommand = "--full-generate-key"

  def commonArguments(homeDirectory: File, statusFileDescriptor: Int): Seq[GpgArgument] = {
    Seq(
      GpgOption.homeDir(homeDirectory),
      GpgFlag.batch,
      GpgFlag.withColon,
      GpgOption.statusFD(statusFileDescriptor)
    )
  }

  def generateKey(gpg: String, options: Seq[String]): Unit = {
    // TODO: Add the remaining options.
    execute(gpg, options, FullGenerateKeyCommand)
  }

  def execute(gpg: String, options: Seq[String], command: String): Unit = {
    val processCommand = gpg +: options :+ command
    val exitCode = processCommand.run().exitValue()
    if (exitCode != 0) {
      throw GpgCommandFailedException(command, exitCode)
    }
  }
}

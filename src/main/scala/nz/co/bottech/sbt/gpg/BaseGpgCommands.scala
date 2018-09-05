package nz.co.bottech.sbt.gpg

import java.io.File

import scala.sys.process._

trait BaseGpgCommands {

  def commonArguments(homeDirectory: File, statusFileDescriptor: Int): Seq[GpgArgument]

  def generateKey(gpg: String, options: Seq[String]): Unit

  def execute(gpg: String, options: Seq[String], command: String): Unit = {
    val processCommand = gpg +: options :+ command
    val exitCode = processCommand.run().exitValue()
    if (exitCode != 0) {
      throw GpgCommandFailedException(command, exitCode)
    }
  }
}

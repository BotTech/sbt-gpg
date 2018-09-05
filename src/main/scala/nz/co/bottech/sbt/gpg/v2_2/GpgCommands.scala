package nz.co.bottech.sbt.gpg.v2_2

import java.io.File

import nz.co.bottech.sbt.gpg._

object GpgCommands extends BaseGpgCommands {

  final val FullGenerateKeyCommand = "--full-generate-key"

  override def commonArguments(homeDirectory: File, statusFileDescriptor: Int): Seq[GpgArgument] = {
    Seq(
      GpgOption.homeDir(homeDirectory),
      GpgFlag.batch,
      GpgFlag.withColon,
      GpgOption.statusFD(statusFileDescriptor)
    )
  }

  override def generateKey(gpg: String, options: Seq[String]): Unit = {
    // TODO: Add the remaining options.
    execute(gpg, options, FullGenerateKeyCommand)
  }
}

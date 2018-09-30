package nz.co.bottech.sbt.gpg.v2_1

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  protected override final val GpgCommandProperty = "gpg.command.v2_1"
  protected override final val GpgCommand = "gpg2"
  protected override final val GpgVersionRegex = "gpg.* 2.1.*"
  protected override final val VersionCommand = "--version"
  protected override final val GenerateKeyCommand = "--full-gen-key"
  protected override final val ListKeysCommand = Seq(
    "--list-keys",
    "--with-keygrip",
    "--with-secret",
    "--fingerprint",
    "--fingerprint"
  )

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    executeVersionCommand(GpgVersion2Dot1, log)
  }
}

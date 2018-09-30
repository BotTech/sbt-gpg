package nz.co.bottech.sbt.gpg.v2_0

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  protected override final val GpgCommandProperty = "gpg.command.v2_0"
  protected override final val GpgCommand = "gpg2"
  protected override final val GpgVersionRegex = "gpg.* 2.0.*"
  protected override final val VersionCommand = "--version"
  protected override final val GenerateKeyCommand = "--gen-key"
  protected override final val ListKeysCommand = Seq("--list-keys", "--fingerprint", "--fingerprint")

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    executeVersionCommand(GpgVersion2Dot0, log)
  }
}

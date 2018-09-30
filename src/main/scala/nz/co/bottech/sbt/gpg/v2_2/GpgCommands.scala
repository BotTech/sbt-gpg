package nz.co.bottech.sbt.gpg.v2_2

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  protected override final val GpgCommandProperty = "gpg.command.v2_2"
  protected override final val GpgCommand = "gpg"
  protected override final val GpgVersionRegex = "gpg.* 2.2.*"
  protected override final val VersionCommand = "--version"
  protected override final val GenerateKeyCommand = "--full-generate-key"
  protected override final val ListKeysCommand = Seq("--list-keys", "--with-keygrip", "--with-secret")

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    executeVersionCommand(GpgVersion2Dot2, log)
  }
}

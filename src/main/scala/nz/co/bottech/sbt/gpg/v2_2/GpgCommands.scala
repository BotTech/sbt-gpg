package nz.co.bottech.sbt.gpg.v2_2

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  protected override final val GpgCommandProperty = "gpg.command.v2_2"
  protected override final val GpgCommand = "gpg"
  protected override final val GpgVersionRegex = "gpg.* 2.2.*"
  protected override final val AddKeyCommand = "--quick-add-key"
  protected override final val ExportSubKeyCommand = "--export-secret-subkeys"
  protected override final val GenerateKeyCommand = "--full-generate-key"
  protected override final val ImportKeyCommand = "--import"
  protected override final val VersionCommand = "--version"
  protected override final val ListKeysCommand = "--list-keys"

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    executeVersionCommand(GpgVersion2Dot2, log)
  }

  override def listKeys(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): Seq[GpgKeyInfo] = {
    val args = Seq(GpgFlag.withKeyGrip, GpgFlag.withSecret).flatMap(_.prepare()) ++ options
    super.listKeys(gpg, args, parameters, log)
  }
}

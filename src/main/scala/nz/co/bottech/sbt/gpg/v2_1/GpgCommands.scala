package nz.co.bottech.sbt.gpg.v2_1

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  protected override final val GpgCommandProperty = "gpg.command.v2_1"
  protected override final val GpgCommand = "gpg2"
  protected override final val GpgVersionRegex = "gpg.* 2.1.*"
  protected override final val AddKeyCommand = "not-supported"
  protected override final val ExportSubKeyCommand = "--export-secret-subkeys"
  protected override final val GenerateKeyCommand = "--full-gen-key"
  protected override final val ImportKeyCommand = "--import"
  protected override final val ListKeysCommand = "--list-keys"
  protected override final val SignCommand = "--detach-sig"
  protected override final val VersionCommand = "--version"

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    executeVersionCommand(GpgVersion2Dot1, log)
  }

  override def listKeys(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): Seq[GpgKeyInfo] = {
    val args = Seq(
      GpgFlag.withKeyGrip,
      GpgFlag.withSecret,
      GpgFlag.fingerprint,
      GpgFlag.fingerprint // Include this twice to get the fingerprint of subkeys.
    ).flatMap(_.prepare()) ++ options
    super.listKeys(gpg, args, parameters, log)
  }

  override def addKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): String = {
    throw new UnsupportedOperationException("GnuPG 2.1 does not support adding a key in batch mode.")
  }
}

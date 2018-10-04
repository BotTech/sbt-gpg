package nz.co.bottech.sbt.gpg.v2_0

import java.io.File

import nz.co.bottech.sbt.gpg._
import sbt.util.Logger

object GpgCommands extends BaseGpgCommands {

  protected override final val GpgCommandProperty = "gpg.command.v2_0"
  protected override final val GpgCommand = "gpg2"
  protected override final val GpgVersionRegex = "gpg.* 2.0.*"
  protected override final val AddKeyCommand = "not-supported"
  protected override final val ExportSubKeyCommand = "--export-secret-subkeys"
  protected override final val GenerateKeyCommand = "--gen-key"
  protected override final val ImportKeyCommand = "--import"
  protected override final val ListKeysCommand = "--list-keys"
  protected override final val SignCommand = "--detach-sig"
  protected override final val VersionCommand = "--version"

  override def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)] = {
    executeVersionCommand(GpgVersion2Dot0, log)
  }

  override def passphraseArguments(file: File): Seq[GpgArgument] = {
    Seq(GpgOption.passphraseFile(file))
  }

  override def listKeys(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): Seq[GpgKeyInfo] = {
    val args = Seq(
      GpgFlag.fingerprint,
      GpgFlag.fingerprint // Include this twice to get the fingerprint of subkeys.
    ).flatMap(_.prepare()) ++ options
    super.listKeys(gpg, args, parameters, log)
  }

  override def addKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): String = {
    throw new UnsupportedOperationException("GnuPG 2.0 does not support adding a key in batch mode.")
  }
}

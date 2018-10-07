package nz.co.bottech.sbt.gpg

import java.io.File

import sbt._

object GpgSigner {

  final val AscType = "asc"
  final val SigType = "asc"

  def messageSignatureFile(message: File, armor: Boolean): sbt.File = {
    if (armor) {
      file(s"${message.getPath}.asc")
    } else {
      file(s"${message.getPath}.sig")
    }
  }

  def passphraseArguments(version: GpgVersion, passphraseFile: File): Seq[GpgArgument] = {
    val commands = GpgVersion.commands(version)
    commands.passphraseArguments(passphraseFile)
  }

  def signArguments(armor: Boolean, keyFingerprint: String, signature: File): Seq[GpgArgument] = {
    val armorArg = if (armor) {
      Seq(GpgFlag.armor)
    } else {
      Seq.empty[GpgArgument]
    }
    armorArg :+
      GpgOption.localUser(keyFingerprint) :+
      GpgOption.output(signature)
  }

  def signer(gpg: String,
             version: GpgVersion,
             passphraseFile: Option[File],
             armor: Boolean,
             keyFingerprint: String,
             options: Seq[String],
             log: Logger)
            (message: File): File = {
    val signature = messageSignatureFile(message, armor)
    sign(gpg, version, passphraseFile, armor, keyFingerprint, signature, options, message, log)
  }

  def sign(gpg: String,
           version: GpgVersion,
           passphraseFile: Option[File],
           armor: Boolean,
           keyFingerprint: String,
           signature: File,
           options: Seq[String],
           message: File,
           log: Logger): File = {
    val passphraseArgs = passphraseFile.toSeq.flatMap(passphraseArguments(version, _))
    val signArgs = signArguments(armor, keyFingerprint, signature)
    val args = passphraseArgs ++ signArgs
    val opts = options ++ args.flatMap(_.prepare())
    GpgVersion.commands(version).sign(gpg, opts, Seq(message.getPath), log)
    signature
  }

  def asc(name: String, artifact: Artifact): Artifact = signature(name, artifact, AscType)

  def sig(name: String, artifact: Artifact): Artifact = signature(name, artifact, SigType)

  private def signature(name: String, artifact: Artifact, typ: String) = {
    Artifact(name, typ, typ, artifact.classifier, artifact.configurations, None)
  }
}

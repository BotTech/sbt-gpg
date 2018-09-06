package nz.co.bottech.sbt.gpg

import sbt._

trait GpgKeys {

  val gpgAdditionalOptions = settingKey[Seq[String]]("Additional options to pass to gpg.")
  val gpgArguments = taskKey[Seq[GpgArgument]]("The arguments to pass to gpg.")
  val gpgCommand = taskKey[String]("The GnuPGP gpg CLI command.")
  val gpgCommandAndVersion = taskKey[(String, GpgVersion)]("The GnuPGP gpg CLI command and version.")
  val gpgGenerateKey = taskKey[Unit]("Generates a new key pair.")
  val gpgHomeDir = settingKey[File]("The gpg home directory.")
  val gpgStatusFileDescriptor = settingKey[Int]("The file descriptor for writing status messages.")
  val gpgVersion = taskKey[GpgVersion]("The version of GnuPGP.")
}

object GpgKeys extends GpgKeys

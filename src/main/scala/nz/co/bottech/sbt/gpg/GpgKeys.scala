package nz.co.bottech.sbt.gpg

import sbt._

trait GpgKeys {

  val gpgArguments = settingKey[Seq[GpgArgument]]("The arguments to pass to gpg.")
  val gpgCommand = settingKey[String]("The GnuPGP gpg CLI command.")
  val gpgGenerateKey = taskKey[Unit]("Generates a new key pair.")
  val gpgHomeDir = settingKey[File]("The gpg home directory.")
  val gpgAdditionalOptions = settingKey[Seq[String]]("Additional options to pass to gpg.")
  val gpgStatusFileDescriptor = settingKey[Int]("The file descriptor for writing status messages.")
  val gpgVersion = settingKey[GpgVersion]("The version of GnuPGP.")
}

object GpgKeys extends GpgKeys

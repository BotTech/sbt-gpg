package nz.co.bottech.sbt.gpg

import sbt._

trait GpgKeys {

  val gpgAdditionalOptions = settingKey[Seq[String]]("Additional options to pass to gpg.")
  val gpgAddKey = taskKey[String]("Adds a subkey to an existing key.")
  val gpgArguments = taskKey[Seq[GpgArgument]]("The arguments to pass to gpg.")
  val gpgArmor = taskKey[Boolean]("Create ASCII armored output.")
  val gpgCommand = taskKey[String]("The GnuPGP gpg CLI command.")
  val gpgCommandAndVersion = taskKey[(String, GpgVersion)]("The GnuPGP gpg CLI command and version.")
  val gpgExpireDate = settingKey[String]("The expiration date for the key (and the subkey).")
  val gpgExportSubKey = taskKey[File]("Exports a subkey without the primary secret key.")
  val gpgGenerateKey = taskKey[String]("Generates a new key pair.")
  val gpgHomeDir = settingKey[Option[File]]("The gpg home directory.")
  val gpgImportKey = taskKey[Unit]("Imports a key to the keyring.")
  val gpgKeyFile = settingKey[File]("The key file.")
  val gpgKeyFingerprint = taskKey[String]("The SHA-1 fingerprint of the key.")
  val gpgKeyLength = settingKey[Int]("The length of the generated key in bits.")
  val gpgKeyType = settingKey[String]("The OpenPGP algorithm number or name to use for the key.")
  val gpgKeyUsage = settingKey[Set[GpgKeyUsage]]("List of key usages.")
  val gpgListKeys = taskKey[Seq[GpgKeyInfo]]("List the existing keys.")
  val gpgMessage = taskKey[File]("A message.")
  val gpgNameReal = settingKey[String]("Name.")
  val gpgNameEmail = settingKey[String]("Email address.")
  val gpgParameters = taskKey[Seq[String]]("Parameters to the command.")
  val gpgParametersFile = taskKey[File]("The parameters file.")
  val gpgPassphrase = settingKey[Option[String]]("Passphrase for the secret key.")
  val gpgPassphraseFile = taskKey[Option[File]]("Passphrase file for the secret key.")
  val gpgSelectPassphrase = taskKey[Option[String]]("Selects the passphrase for the secret key either from gpgPassphrase or the ~/.sbt/.credentials file.")
  val gpgSign = taskKey[File]("Sign a message.")
  val gpgSignatureFile = taskKey[File]("The signature file.")
  val gpgStatusFileDescriptor = settingKey[Int]("The file descriptor for writing status messages.")
  val gpgSubkeyLength = settingKey[Int]("The length of the generated subkey in bits.")
  val gpgSubkeyType = settingKey[String]("The OpenPGP algorithm number or name to use for the subkey.")
  val gpgSubkeyUsage = settingKey[Set[GpgKeyUsage]]("List of subkey usages.")
  val gpgVersion = taskKey[GpgVersion]("The version of GnuPGP.")
}

object GpgKeys extends GpgKeys

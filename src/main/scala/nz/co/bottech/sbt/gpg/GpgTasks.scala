package nz.co.bottech.sbt.gpg

import java.nio.charset.StandardCharsets
import java.nio.file.attribute.{PosixFilePermission, PosixFilePermissions}

import nz.co.bottech.sbt.gpg.BaseGpgCommands.CommandOutput
import nz.co.bottech.sbt.gpg.GpgErrors._
import nz.co.bottech.sbt.gpg.GpgKeys._
import sbt.Keys._
import sbt._
import sbt.Classpaths._

import scala.collection.JavaConverters._

object GpgTasks {

  // Max socket length is usually 108 bytes on Linux and 104 bytes on macOS.
  // -1 for the null character.
  // -12 for the /S.gpg-agent suffix.
  final val MaxHomeDirLength = 104 - 1 - 12

  final val HomeDirPermissions: Set[PosixFilePermission] = {
    PosixFilePermissions.fromString("rwx------").asScala.toSet
  }

  final val ErrorChangingPassphraseRegex = ".*error changing passphrase: (.*)".r

  // Workaround for https://github.com/sbt/sbt/issues/3110
  final val Def = sbt.Def

  def gpgCommandAndVersionTask: Def.Initialize[Task[(String, GpgVersion)]] = Def.task {
    val log = state.value.log
    val commandAndVersion = for {
      error1 <- v2_2.GpgCommands.commandAndVersion(log).swap
      error2 <- v2_1.GpgCommands.commandAndVersion(log).swap
      error3 <- v2_0.GpgCommands.commandAndVersion(log).swap
    } yield {
      val messages = Seq(
        "Failed to detect GnuPG version.",
        error1.getMessage,
        error2.getMessage,
        error3.getMessage
      )
      GpgUnknownVersionException(messages.mkString("\n"))
    }
    commandAndVersion match {
      case Left(result) => result
      case Right(error) => throw error
    }
  }

  def gpgArgumentsTask = Def.task {
    val log = state.value.log
    val commands = GpgVersion.commands(gpgVersion.value)
    val dir = gpgHomeDir.value
    dir.filter(_.getPath.getBytes.length > MaxHomeDirLength).foreach { d =>
      log.warn(s"Home directory $d is over the maximum length of $MaxHomeDirLength.")
      log.warn("gpg-agent may fail to start.")
    }
    val debug = logLevel.?.value.contains(Level.Debug)
    commands.commonArguments(dir, gpgStatusFileDescriptor.value, debug)
  }

  def gpgSelectPassphraseTask = Def.task {
    gpgPassphrase.value orElse Credentials.forHost(credentials.value, "gpg").map(_.passwd)
  }

  private def gpgNameTask = Def.task {
    val name = GpgName(
      real = GpgSettings.mandatoryTask(gpgNameReal).value,
      email = GpgSettings.mandatoryTask(gpgNameEmail).value
    )
    require(name.real.trim.nonEmpty, "gpgNameReal must not be empty.")
    require(name.email.trim.nonEmpty, "gpgNameEmail must not be empty.")
    name
  }

  def gpgParametersFileTask = Def.task {
    val log = state.value.log
    val parameters = GpgParameters(
      key = GpgKeyParameters(
        length = gpgKeyLength.value,
        typ = gpgKeyType.value,
        usage = gpgKeyUsage.value
      ),
      subkey = GpgKeyParameters(
        length = gpgSubkeyLength.value,
        typ = gpgSubkeyType.value,
        usage = gpgSubkeyUsage.value
      ),
      expire = gpgExpireDate.value,
      name = gpgNameTask.value,
      passphrase = gpgSelectPassphrase.value
    )
    val file = target.value / ".gnupg" / "parameters"
    file.deleteOnExit()
    GpgParameterFile.create(parameters, file, log)
  }

  def formatSubkeyFingerprint(fingerprint: String): String = {
    if (fingerprint.endsWith("!")) {
      fingerprint
    } else {
      fingerprint + "!"
    }
  }

  def gpgPassphraseFileTask = Def.task {
    val log = state.value.log
    val maybePassphrase = gpgSelectPassphrase.value
    val file = target.value / ".gnupg" / "passphrase"
    maybePassphrase.map { passphrase =>
      file.deleteOnExit()
      sbt.IO.write(file, passphrase, StandardCharsets.UTF_8, append = false)
      log.debug(s"Parameters file written to $file.")
      file
    }
  }

  def generateKeyTask: Def.Initialize[Task[String]] = {
    runCommandTask(GpgVersion.commands(_).generateKey)
  }

  def listKeysTask: Def.Initialize[Task[Seq[GpgKeyInfo]]] = {
    runCommandTask(GpgVersion.commands(_).listKeys)
  }

  def addKeyTask: Def.Initialize[Task[String]] = {
    runCommandTask(GpgVersion.commands(_).addKey)
  }

  def passphraseArgumentsTask: Def.Initialize[Task[Seq[GpgArgument]]] = Def.task {
    val commands = GpgVersion.commands(gpgVersion.value)
    gpgPassphraseFile.value.toSeq.flatMap { file =>
      commands.passphraseArguments(file)
    }
  }

  def addKeyParametersTask: Def.Initialize[Task[Seq[String]]] = Def.task {
    val fpr = (gpgKeyFingerprint ?? "default").value
    val keyType = gpgSubkeyType.value
    val algo = if (keyType.isEmpty) {
      "default"
    } else {
      keyType + gpgSubkeyLength.value
    }
    val keyUsage = gpgSubkeyUsage.value
    val usage = if (keyUsage.isEmpty) {
      "default"
    } else {
      gpgSubkeyUsage.value.mkString(",")
    }
    Seq(fpr, algo, usage, gpgExpireDate.value)
  }

  def exportKeyTask: Def.Initialize[Task[File]] = Def.taskDyn {
    val keyFile = prepareKeyFileTask.value
    Def.task {
      val _ = runCommandTask(GpgVersion.commands(_).exportKey).value
      keyFile
    }
  }

  def exportSubkeyTask: Def.Initialize[Task[File]] = Def.taskDyn {
    val keyFile = prepareKeyFileTask.value
    Def.task {
      val _ = runCommandTask(GpgVersion.commands(_).exportSubkey).value
      keyFile
    }
  }

  def prepareKeyFileTask = Def.task {
    val file = gpgKeyFile.value
    val parentDir = Option(file.getParentFile)
    parentDir.foreach(_.mkdirs())
    file.delete()
    file
  }

  def exportArgumentsTask: Def.Initialize[Task[Seq[GpgArgument]]] = Def.task {
    val armor = if (gpgArmor.value) {
      Seq(GpgFlag.armor)
    } else {
      Seq.empty[GpgArgument]
    }
    gpgArguments.value ++
      passphraseArgumentsTask.value ++
      armor :+
      GpgOption.output(gpgKeyFile.value)
  }

  def importKeyTask: Def.Initialize[Task[Unit]] = Def.task {
    runCommandTask(GpgVersion.commands(_).importKey).value
  }

  def signArgumentsTask: Def.Initialize[Task[Seq[GpgArgument]]] = Def.task {
    val armor = if (gpgArmor.value) {
      Seq(GpgFlag.armor)
    } else {
      Seq.empty[GpgArgument]
    }
    gpgArguments.value ++
      passphraseArgumentsTask.value ++
      armor :+
      GpgOption.localUser(GpgSettings.mandatoryTask(gpgKeyFingerprint).value) :+
      GpgOption.output(GpgSettings.mandatoryTask(gpgSignatureFile).value)
  }

  def signatureFileTask = Def.task {
    val message = GpgSettings.mandatoryTask(gpgMessage).value
    GpgSigner.messageSignatureFile(message, gpgArmor.value)
  }

  def signTask: Def.Initialize[Task[File]] = Def.task {
    runCommandTask(GpgVersion.commands(_).sign).value
    GpgSettings.mandatoryTask(gpgSignatureFile).value
  }

  def signerTask: Def.Initialize[Task[File => File]] = Def.task {
    val log = state.value.log
    val gpg = gpgCommand.value
    val version = gpgVersion.value
    val passphraseFile = gpgPassphraseFile.value
    val armor = gpgArmor.value
    val keyFingerprint = GpgSettings.mandatoryTask(gpgKeyFingerprint).value
    val args = gpgArguments.value
    val additionalOptions = gpgAdditionalOptions.value
    val options = args.flatMap(_.prepare()) ++ additionalOptions
    GpgSigner.signer(gpg, version, passphraseFile, armor, keyFingerprint, options, log)
  }

  def signedArtifactsTask = Def.task {
    val artifacts = packagedArtifacts.value
    val armor = (gpgSigner / gpgArmor).value
    val signer = gpgSigner.value
    val sign = gpgSignArtifacts.value
    if (sign) {
      val signatureArtifact = if (armor) {
        GpgSigner.asc _
      } else {
        GpgSigner.sig _
      }
      artifacts.flatMap {
        case (art, file) =>
          val signature = signer(file)
          Seq(
            art -> file,
            signatureArtifact(signature.getName, art) -> signature
          )
      }
    } else {
      artifacts
    }
  }

  def resolverNameTask = Def.task {
    val publishToOption = publishTo.value
    if (publishArtifact.value) getPublishTo(publishToOption).name else "local"
  }

  def publishConfigurationTask = Def.task {
    publishConfig(
      publishMavenStyle.value,
      deliverPattern(crossTarget.value),
      if (isSnapshot.value) "integration" else "release",
      ivyConfigurations.value.map(c => ConfigRef(c.name)).toVector,
      gpgSignedArtifacts.value.toVector,
      checksums.value.toVector,
      gpgResolverName.value,
      ivyLoggingLevel.value,
      isSnapshot.value
    )
  }

  def editKeyTask: Def.Initialize[Task[CommandOutput]] = {
    runCommandTask(GpgVersion.commands(_).editKey)
  }

  def tempTargetDirTask: Def.Initialize[Task[File]] = Def.task {
    val targetDir = target.value
    val dir = IO.createUniqueDirectory(targetDir)
    dir.deleteOnExit()
    dir.setPermissions(HomeDirPermissions)
    dir
  }

  def changePassphraseParametersTask: Def.Initialize[Task[Seq[String]]] = Def.task {
    val fpr = GpgSettings.mandatoryTask(gpgKeyFingerprint).value
    Seq(fpr, "passwd", "quit")
  }

  def changeKeyPassphraseTask: Def.Initialize[Task[Unit]] = Def.taskDyn {
    val keyFingerprint = GpgSettings.mandatoryTask(gpgKeyFingerprint).value
    changePassphraseTask(editKeyPassphraseTask, GpgVersion.commands(_).exportKey, keyFingerprint)
  }

  def changeSubkeyPassphraseTask: Def.Initialize[Task[Unit]] = Def.taskDyn {
    val keyFingerprint = formatSubkeyFingerprint(GpgSettings.mandatoryTask(gpgKeyFingerprint).value)
    changePassphraseTask(editSubkeyPassphraseTask, GpgVersion.commands(_).exportSubkey, keyFingerprint)
  }

  private def editKeyPassphraseTask = {
    val missingKeyMessage = "Cannot change the passphrase of a key without the primary secret key."
    editAnyKeyPassphraseTask(missingKeyMessage)
  }

  private def editSubkeyPassphraseTask = {
    val missingKeyMessage = "Cannot change the passphrase of a subkey without both the primary secret key and the subkey secret key."
    editAnyKeyPassphraseTask(missingKeyMessage)
  }

  private def editAnyKeyPassphraseTask(missingKeyMessage: String) = Def.task {
    val output = editKeyTask.value
    output.err.foreach { err =>
      if (err.contains("Need the secret key to do this")) {
        throw GpgMissingSecretKey(missingKeyMessage)
      }
      err match {
        case ErrorChangingPassphraseRegex(message) => throw GpgErrorChangingPassphrase(message)
        case _ => // Do nothing
      }
    }
  }

  private def changePassphraseTask(changePassphrase: Def.Initialize[Task[Unit]],
                                   exportCommand: GpgVersion => Command[CommandOutput],
                                   exportKeyFingerprintParam: String) = Def.taskDyn {
    val log = state.value.log
    val gpg = gpgCommand.value
    val version = gpgVersion.value
    val additionalOptions = gpgAdditionalOptions.value
    val keyFile = gpgKeyFile.value
    val hash = fileHash(keyFile)
    val importTask = Def.task {
      val args = gpgArguments.value
      val options = args.flatMap(_.prepare()) ++ additionalOptions
      GpgVersion.commands(version).importKey(gpg, options, Seq(keyFile.getPath), log)
    }
    val exportTask = Def.task {
      val exportArgs = exportArgumentsTask.value
      val options = exportArgs.flatMap(_.prepare()) ++ additionalOptions
      val output = exportCommand(version)(gpg, options, Seq(exportKeyFingerprintParam), log)
      checkChangePassphraseResults(keyFile, hash)
      output
    }
    Def.sequential(importTask, changePassphrase, exportTask, deleteHomeDirTask)
  }

  private def deleteHomeDirTask: Def.Initialize[Task[Unit]] = Def.task {
    gpgHomeDir.value.foreach(IO.delete)
  }

  private def fileHash(file: File): Option[Array[Byte]] = {
    if (file.exists()) {
      Some(Hash(file))
    } else {
      None
    }
  }

  private def checkChangePassphraseResults(file: File, hash: Option[Array[Byte]]): Unit = {
    if (!file.exists()) {
      throw GpgMissingKeyFile("Key file not found.")
    }
    hash match {
      case Some(initialHash) => if (initialHash sameElements Hash(file)) {
        throw GpgPassphraseNotChanged("Passphrase of key was not changed.")
      }
      case None => ()
    }
  }

  type Command[A] = (String, Seq[String], Seq[String], Logger) => A

  def runCommandTask[A](command: GpgVersion => Command[A]): Def.Initialize[Task[A]] = Def.task {
    val log = state.value.log
    val gpg = gpgCommand.value
    val version = gpgVersion.value
    val args = gpgArguments.value
    val additionalOptions = gpgAdditionalOptions.value
    val parameters = gpgParameters.value
    val options = args.flatMap(_.prepare()) ++ additionalOptions
    command(version)(gpg, options, parameters, log)
  }
}

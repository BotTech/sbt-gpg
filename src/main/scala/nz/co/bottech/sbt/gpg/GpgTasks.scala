package nz.co.bottech.sbt.gpg

import java.nio.charset.StandardCharsets

import nz.co.bottech.sbt.gpg.GpgErrors.GpgUnknownVersionException
import nz.co.bottech.sbt.gpg.GpgKeys._
import sbt.Keys._
import sbt.{Def, _}

object GpgTasks {

  // Max socket length is usually 108 bytes on Linux and 104 bytes on macOS.
  // -1 for the null character.
  // -12 for the /S.gpg-agent suffix.
  final val MaxHomeDirLength = 104 - 1 - 12

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
      real = gpgNameReal.value,
      email = gpgNameEmail.value
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
    val fpr = gpgKeyFingerprint.value.getOrElse("default")
    val keyType = gpgKeyType.value
    val algo = if (keyType.isEmpty) {
      "default"
    } else {
      keyType + gpgKeyLength.value
    }
    val keyUsage = gpgKeyUsage.value
    val usage = if (keyUsage.isEmpty) {
      "default"
    } else {
      gpgKeyUsage.value.mkString(",")
    }
    Seq(fpr, algo, usage, gpgExpireDate.value)
  }

  def exportSubKeyTask: Def.Initialize[Task[File]] = Def.task {
    val _ = runCommandTask(GpgVersion.commands(_).exportSubKey).value
    gpgKeyFile.value
  }

  def exportArgumentsTask: Def.Initialize[Task[Seq[GpgArgument]]] = Def.task {
    val armor = if (gpgArmor.value) {
      Seq(GpgFlag.armor)
    } else {
      Seq.empty[GpgArgument]
    }
    passphraseArgumentsTask.value ++
      armor ++
      gpgArguments.value :+
      GpgOption.output(gpgKeyFile.value)
  }

  def importKeyTask: Def.Initialize[Task[Unit]] = Def.task {
    runCommandTask(GpgVersion.commands(_).importKey).value
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

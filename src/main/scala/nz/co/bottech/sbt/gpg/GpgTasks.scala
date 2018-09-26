package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import sbt.{Def, _}
import sbt.Keys._

object GpgTasks {

  // Max socket length is usually 108 bytes.
  // -1 for the null character.
  // -12 for the /S.gpg-agent suffix.
  final val MaxHomeDirLength = 108 - 1 - 12

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
    if (dir.getPath.length() > MaxHomeDirLength) {
      log.warn(s"Home directory $dir is over the maximum length of $MaxHomeDirLength.")
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
    val file = gpgHomeDir.value / "parameters"
    file.deleteOnExit()
    GpgParameterFile.create(parameters, file, log)
  }

  def generateKeyTask: Def.Initialize[Task[Unit]] = {
    runCommandTask(GpgVersion.commands(_).generateKey)
  }

  def runCommandTask[A](command: GpgVersion => (String, Seq[String], Seq[String], Logger) => A): Def.Initialize[Task[Unit]] = Def.task {
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

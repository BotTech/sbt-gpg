package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import sbt.{Def, _}
import sbt.Keys._

object GpgTasks {

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
    val commands = GpgVersion.commands(gpgVersion.value)
    commands.commonArguments(
      gpgHomeDir.value,
      gpgStatusFileDescriptor.value
    )
  }

  def generateKeyTask: Def.Initialize[Task[Unit]] = {
    runCommandTask(gpgGenerateKey, GpgVersion.commands(_).generateKey)
  }

  def runCommandTask[A](commandKey: TaskKey[A],
                        command: GpgVersion => (String, Seq[String], Logger) => A): Def.Initialize[Task[Unit]] = Def.task {
    val log = state.value.log
    val gpg = (gpgCommand in commandKey).value
    val version = (gpgVersion in commandKey).value
    val args = (gpgArguments in commandKey).value
    val additionalOptions = (gpgAdditionalOptions in commandKey).value
    val options = args.flatMap(_.toOptions) ++ additionalOptions
    command(version)(gpg, options, log)
  }
}

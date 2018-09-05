package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import sbt.{Def, _}

object GpgTasks {

  def generateKeyTask: Def.Initialize[Task[Unit]] = {
    runCommandTask(gpgGenerateKey, commands(_).generateKey)
  }

  def commands(version: GpgVersion): BaseGpgCommands = version match {
    case GpgVersion2Dot0 => ???
    case GpgVersion2Dot1 => ???
    case GpgVersion2Dot2 => v2_2.GpgCommands
  }

  def runCommandTask[A](commandKey: TaskKey[A],
                        command: GpgVersion => (String, Seq[String]) => A): Def.Initialize[Task[Unit]] = Def.task {
    val gpg = (gpgCommand in commandKey).value
    val version = (gpgVersion in commandKey).value
    val args = (gpgArguments in commandKey).value
    val additionalOptions = (gpgAdditionalOptions in commandKey).value
    val options = args.flatMap(_.toOptions) ++ additionalOptions
    command(version)(gpg, options)
  }
}

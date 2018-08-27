package nz.co.bottech.sbt

import nz.co.bottech.sbt.GpgKeys._
import sbt.{Def, _}

object GpgTasks {

  def generateKeyTask: Def.Initialize[Task[Unit]] = {
    commandTask(gpgGenerateKey, GpgCommands.generateKey)
  }

  def commandTask[A](commandKey: TaskKey[A],
                     command: (String, Seq[String]) => A): Def.Initialize[Task[Unit]] = Def.task {
    val gpg = (gpgCommand in commandKey).value
    val args = (gpgArguments in commandKey).value
    val additionalOptions = (gpgAdditionalOptions in commandKey).value
    val options = args.flatMap(_.toOptions) ++ additionalOptions
    GpgCommands.generateKey(gpg, options)
  }
}

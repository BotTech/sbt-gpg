package nz.co.bottech.sbt

import nz.co.bottech.sbt.GpgKeys._
import nz.co.bottech.sbt.GpgTasks._
import sbt._
import sbt.Keys._

object GpgSettings {

  val rawSettings = Seq(
    gpgArguments := gpgArgumentsSetting.value,
    gpgCommand := "gpg",
    gpgGenerateKey := generateKeyTask.value,
    gpgHomeDir := target.value / ".gnupg",
    gpgAdditionalOptions := Seq.empty,
    gpgStatusFileDescriptor := 1
  )

  def gpgArgumentsSetting = Def.setting {
    GpgCommands.commonArguments(
      gpgHomeDir.value,
      gpgStatusFileDescriptor.value
    )
  }
}

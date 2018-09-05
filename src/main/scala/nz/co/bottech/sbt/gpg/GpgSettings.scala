package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import nz.co.bottech.sbt.gpg.GpgTasks._
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
    gpgVersion.value match {
      case GpgVersion2Dot0 => ???
      case GpgVersion2Dot1 => ???
      case GpgVersion2Dot2 =>
        v2_2.GpgCommands.commonArguments(
          gpgHomeDir.value,
          gpgStatusFileDescriptor.value
        )
    }
  }
}

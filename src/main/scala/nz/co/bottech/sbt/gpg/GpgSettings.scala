package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import nz.co.bottech.sbt.gpg.GpgTasks._
import sbt._
import sbt.Keys._

object GpgSettings {

  val rawSettings = Seq(
    gpgAdditionalOptions := Seq.empty,
    gpgArguments := gpgArgumentsTask.value,
    gpgCommand := gpgCommandAndVersion.value._1,
    gpgCommandAndVersion := gpgCommandAndVersionTask.value,
    gpgGenerateKey := generateKeyTask.value,
    gpgHomeDir := target.value / ".gnupg",
    gpgStatusFileDescriptor := 1,
    gpgVersion := gpgCommandAndVersion.value._2
  )
}

import nz.co.bottech.sbt.gpg._
import sbt.librarymanagement.ivy.Credentials

scalaVersion := "2.12.6"

gpgNameReal := "Jim Bob"
gpgNameEmail := "jim.bob@example.com"
credentials += Credentials("GnuPG Realm", "gpg", "", "topsecret")

gpgArguments := {
  GpgTasks.gpgArgumentsTask.value.map {
    case option@GpgOption(GpgOption.homeDir.option, _) => option.copy(value = () => "/tmp/.gnupg")
    case option => option
  }
}

gpgParametersFile := {
  file("/") / "tmp" / ".gnupg" / gpgParametersFile.value.getName
}

TaskKey[Unit]("check") := {
  val log = state.value.log
  log.info("running gpgGenerateKey task")
  gpgGenerateKey.value
}

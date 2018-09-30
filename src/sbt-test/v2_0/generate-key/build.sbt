import nz.co.bottech.sbt.gpg._

scalaVersion := "2.12.6"

gpgNameReal := "Jim Bob"
gpgNameEmail := "jim.bob@example.com"
gpgPassphrase := Some("oh no this shouldn't be here")

gpgParametersFile := {
  file("/") / "root" / ".gnupg" / gpgParametersFile.value.getName
}

TaskKey[Unit]("check") := {
  val log = state.value.log
  log.info("running gpgGenerateKey task")
  gpgGenerateKey.value
}

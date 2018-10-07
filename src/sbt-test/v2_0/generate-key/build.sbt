import nz.co.bottech.sbt.gpg._

scalaVersion := "2.12.6"

inTask(gpgGenerateKey) {
  Seq(
    gpgNameReal := "Jim Bob",
    gpgNameEmail := "jim.bob@example.com",
    gpgSelectPassphrase := Some("password123"),
    gpgParametersFile := {
      file("/") / "root" / ".gnupg" / gpgParametersFile.value.getName
    }
  )
}

TaskKey[Unit]("check") := {
  gpgGenerateKey.value
}

import nz.co.bottech.sbt.gpg._

scalaVersion := "2.12.6"

credentials += Credentials("GnuPG Realm", "gpg", "", "topsecret")

inTask(gpgGenerateKey) {
  Seq(
    gpgNameReal := "Jim Bob",
    gpgNameEmail := "jim.bob@example.com",
    gpgParametersFile := {
      file("/") / "root" / ".gnupg" / gpgParametersFile.value.getName
    }
  )
}

TaskKey[Unit]("check") := {
  gpgGenerateKey.value
}

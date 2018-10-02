import nz.co.bottech.sbt.gpg._

scalaVersion := "2.12.6"

gpgNameReal := "Jim Bob"
gpgNameEmail := "jim.bob@example.com"
credentials += Credentials("GnuPG Realm", "gpg", "", "topsecret")

gpgParametersFile := {
  file("/") / "root" / ".gnupg" / gpgParametersFile.value.getName
}

TaskKey[Unit]("check") := {
  gpgGenerateKey.value
}

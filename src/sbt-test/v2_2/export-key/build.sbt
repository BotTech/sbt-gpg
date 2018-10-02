scalaVersion := "2.12.6"

gpgKeyFingerprint := Some("66CA2BF946D1863DF30E98407102E744704372FE")
gpgPassphrase := Some("password123")
gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
  file("/") / "root" / ".gnupg" / f.getName
}
gpgExportSubKey / gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"

TaskKey[Unit]("check") := {
  gpgExportSubKey.value
}
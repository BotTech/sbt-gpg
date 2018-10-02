scalaVersion := "2.12.6"

gpgKeyFingerprint := Some("426497E0A89864CD2B0E83B394CC94C6B2059D2F")
gpgPassphrase := Some("password123")
gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
  file("/") / "root" / ".gnupg" / f.getName
}
gpgExportSubKey / gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"

TaskKey[Unit]("check") := {
  gpgExportSubKey.value
}

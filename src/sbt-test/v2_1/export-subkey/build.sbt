scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgExportSubkey) {
  Seq(
    gpgKeyFingerprint := "426497E0A89864CD2B0E83B394CC94C6B2059D2F",
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

TaskKey[Unit]("check") := {
  gpgExportSubkey.value
}

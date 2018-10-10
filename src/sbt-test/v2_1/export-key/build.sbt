scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgExportSubkey) {
  Seq(
    gpgKeyFingerprint := "D280CAFCE49A316AB1E44F0FD9590422EEA48955",
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

TaskKey[Unit]("check") := {
  gpgExportSubkey.value
}

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgExportKey) {
  Seq(
    gpgKeyFingerprint := "E6E48F840A1BCD2FA67F50E1815E51E2D58B34ED",
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

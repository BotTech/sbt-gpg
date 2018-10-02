scalaVersion := "2.12.6"

gpgKeyFingerprint := Some("61C0F2973D7A4C8D6620A957EA4CB4E3E4DE6261")
gpgPassphrase := Some("password123")
gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
  file("/") / "root" / ".gnupg" / f.getName
}
gpgExportSubKey / gpgOutputFile := file("/") / "root" / ".gnupg" / "exported-key.gpg"

TaskKey[Unit]("check") := {
  val log = state.value.log
  log.info("running gpgExportSubKey task")
  gpgExportSubKey.value
}

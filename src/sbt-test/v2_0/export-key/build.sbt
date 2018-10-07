scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgExportSubkey) {
  Seq(
    gpgKeyFingerprint := "61C0F2973D7A4C8D6620A957EA4CB4E3E4DE6261",
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

TaskKey[Unit]("check") := {
  val log = state.value.log
  log.info("running gpgExportSubkey task")
  gpgExportSubkey.value
}

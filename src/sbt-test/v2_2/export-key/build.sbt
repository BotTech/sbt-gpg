scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgExportSubKey) {
  Seq(
    gpgKeyFingerprint := "66CA2BF946D1863DF30E98407102E744704372FE",
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

TaskKey[Unit]("check") := {
  gpgExportSubKey.value
}

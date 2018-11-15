scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgExportSubkey) {
  Seq(
    gpgKeyFingerprint := "D152F3F412C7235D8240361970CEB1BC56395D7E",
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

TaskKey[Unit]("check") := {
  .value
}

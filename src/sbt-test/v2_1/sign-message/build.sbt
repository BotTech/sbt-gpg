scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgSign) {
  Seq(
    gpgKeyFingerprint := "3E96D598CEC6F6393BF46BB6F4A83E074124E0F9",
    gpgPassphraseFile := Some {
      val f = gpgPassphraseFile.value.get
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgMessage := file("/") / "root" / ".gnupg" / "message.txt"
  )
}

TaskKey[Unit]("check") := {
  gpgSign.value
}

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgSign) {
  Seq(
    gpgKeyFingerprint := "1F5B17A1D9EEB0F9221704A58D1C95A18943856B",
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

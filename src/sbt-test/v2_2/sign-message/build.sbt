scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgSign) {
  Seq(
    gpgKeyFingerprint := "66CA2BF946D1863DF30E98407102E744704372FE",
    gpgPassphraseFile := Some {
      val f = gpgPassphraseFile.value.get
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgSignatureFile := file("/") / "root" / ".gnupg" / "message.sig",
    gpgMessage := file("/") / "root" / ".gnupg" / "message.txt"
  )
}

TaskKey[Unit]("check") := {
  gpgSign.value
}

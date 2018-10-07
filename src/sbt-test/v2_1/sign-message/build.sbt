import sbt.util

scalaVersion := "2.12.6"

logLevel := util.Level.Debug

gpgPassphrase := Some("password123")

inTask(gpgSign) {
  Seq(
    gpgKeyFingerprint := "77F56521010ACE05AB9184A345D39C04A5481116",
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

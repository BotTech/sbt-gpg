scalaVersion := "2.12.6"

gpgPassphrase := Some("barfoo123")
publishLocal / gpgSignArtifacts := true
gpgKeyFingerprint := "FEC82270648E886FEFBA0EABE9E0393F58CBAEA5!"

inTask(gpgImportKey) {
  Seq(
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

packagedArtifacts := {
  val baseDir = baseDirectory.value
  packagedArtifacts.value.mapValues {
    Path.rebase(baseDir, file("/") / "root" / "sbt-gpg").andThen(_.get)
  }
}

inTask(gpgSigner) {
  Seq(
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    }
  )
}

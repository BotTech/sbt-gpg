scalaVersion := "2.12.6"

gpgPassphrase := Some("barfoo123")
publishLocal / gpgSignArtifacts := true
gpgKeyFingerprint := "B9A633DBD1A309DB71ED55940E839DDD93691327"

inTask(gpgImportKey) {
  Seq(
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

inTask(gpgTrustKey) {
  Seq(
    gpgCommandFile := {
      file("/") / "root" / ".gnupg" / gpgCommandFile.value.getName
    },
    gpgKeyFingerprint := "DE29CBE0AC9B2EB810E694D7B6A8B64B909CAF2F"
  )
}

inTask(gpgSigner) {
  Seq(
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    }
  )
}

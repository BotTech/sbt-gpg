import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgSigner) {
  Seq(
    gpgKeyFingerprint := "77F56521010ACE05AB9184A345D39C04A5481116",
    gpgPassphraseFile := Some {
      val f = gpgPassphraseFile.value.get
      file("/") / "root" / ".gnupg" / f.getName
    },
    )
}

packagedArtifacts := {
  val baseDir = baseDirectory.value
  packagedArtifacts.value.mapValues {
    Path.rebase(baseDir, file("/") / "root" / "sbt-gpg").andThen(_.get)
  }
}

TaskKey[Unit]("check") := {
  val artifacts = packagedArtifacts.value
  val signedArtifacts = gpgSignedArtifacts.value
  val signatures = artifacts.mapValues(f => file(s"${f.getPath}.asc"))
  require(signedArtifacts.values.toSet === artifacts.values.toSet ++ signatures.values)
}

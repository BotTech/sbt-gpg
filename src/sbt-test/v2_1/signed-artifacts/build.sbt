import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgSigner) {
  Seq(
    gpgKeyFingerprint := "3E96D598CEC6F6393BF46BB6F4A83E074124E0F9",
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
  val signatures = artifacts.map {
    case (a, f) => a.withExtension("asc") -> file(s"${f.getPath}.asc")
  }
  require((artifacts ++ signatures).toSet === signedArtifacts.toSet)
}

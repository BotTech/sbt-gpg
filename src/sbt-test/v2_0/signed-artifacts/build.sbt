import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgSigner) {
  Seq(
    gpgKeyFingerprint := "1F5B17A1D9EEB0F9221704A58D1C95A18943856B",
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
    case (a, f) => a.withExtension(s"${a.extension}.asc") -> file(s"${f.getPath}.asc")
  }
  require((artifacts ++ signatures).toSet === signedArtifacts.toSet)
}

import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgSigner) {
  Seq(
    gpgKeyFingerprint := "66CA2BF946D1863DF30E98407102E744704372FE",
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

import org.scalactic.Requirements._
import org.scalactic.TripleEquals._

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

val messageFile = file("/") / "root" / ".gnupg" / "message.txt"
val messageFileSig = file("/") / "root" / ".gnupg" / "message.txt.asc"

inTask(publishLocal) {
  Seq(
    gpgSignArtifacts := true,
    packagedArtifacts := Map(Artifact("message.txt") -> messageFile)
  )
}

TaskKey[Unit]("check") := {
  val artifacts = publishLocalConfiguration.value.artifacts.map {
    case (_, file) => file
  }
  require(artifacts === Seq(messageFile, messageFileSig))
}

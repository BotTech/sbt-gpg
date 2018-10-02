import nz.co.bottech.sbt.gpg.GpgListingParser.Capability._
import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")
gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
  file("/") / "root" / ".gnupg" / f.getName
}
gpgImportKey / gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"

TaskKey[Unit]("check") := {
  val keys = Def.taskDyn {
    val _ = gpgImportKey.value
    Def.task {
      gpgListKeys.value
    }
  }.value
  val secondSignSubKey = keys(1).subKeys(1)
  require(secondSignSubKey.keyLength === gpgKeyLength.value)
  require(secondSignSubKey.algorithm === 1)
  require(secondSignSubKey.capabilities === Set(Sign))
  require(secondSignSubKey.fingerprint === "92AD2D01E17283B6E3E07FEF00E4371F46DC5AD8")
}

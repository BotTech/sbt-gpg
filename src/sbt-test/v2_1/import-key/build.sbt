import nz.co.bottech.sbt.gpg.GpgListingParser.Capability._
import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

inTask(gpgImportKey) {
  Seq(
    gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
      file("/") / "root" / ".gnupg" / f.getName
    },
    gpgKeyFile := file("/") / "root" / ".gnupg" / "key.asc"
  )
}

TaskKey[Unit]("check") := {
  val keys = Def.taskDyn {
    val _ = gpgImportKey.value
    Def.task {
      gpgListKeys.value
    }
  }.value
  val secondSignSubkey = keys(1).subkeys(1)
  require(secondSignSubkey.keyLength === gpgKeyLength.value)
  require(secondSignSubkey.algorithm === 1)
  require(secondSignSubkey.capabilities === Set(Sign))
  require(secondSignSubkey.fingerprint === "4BF5394A7D46FE51D94646A19A1A3AE860D61D56")
}

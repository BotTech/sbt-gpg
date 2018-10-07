import java.time.Duration
import java.time.temporal.ChronoUnit

import nz.co.bottech.sbt.gpg._
import nz.co.bottech.sbt.gpg.GpgListingParser._
import nz.co.bottech.sbt.gpg.GpgListingParser.Capability._
import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgKeyFingerprint := "3F43DA9CAB5977759FC2E555709CF2B6FF067DEB"
gpgKeyType := "RSA"
gpgKeyLength := 4096
gpgKeyUsage := Set(GpgKeyUsage.sign)
gpgExpireDate := "30d"
gpgPassphrase := Some("password123")
gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
  file("/") / "root" / ".gnupg" / f.getName
}

TaskKey[Unit]("check") := {
  val (fingerprint, keys) = Def.taskDyn {
    val fingerprint = gpgAddKey.value
    Def.task {
      val keys = gpgListKeys.value
      (fingerprint, keys)
    }
  }.value
  val secondSignSubkey = keys(1).subkeys(1)
  require(secondSignSubkey.keyLength === gpgKeyLength.value)
  require(secondSignSubkey.algorithm === 1)
  require(secondSignSubkey.capabilities === Set(Sign))
  require(secondSignSubkey.fingerprint === fingerprint)
}

import java.time.Duration
import java.time.temporal.ChronoUnit

import nz.co.bottech.sbt.gpg._
import nz.co.bottech.sbt.gpg.GpgListingParser._
import nz.co.bottech.sbt.gpg.GpgListingParser.Capability._
import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgKeyFingerprint := Some("3F43DA9CAB5977759FC2E555709CF2B6FF067DEB")
gpgKeyType := "RSA"
gpgKeyLength := 4096
gpgKeyUsage := Set(GpgKeyUsage.sign)
gpgExpireDate := "30d"
gpgPassphrase := Some("password123")
gpgPassphraseFile := gpgPassphraseFile.value.map { f =>
  file("/") / "root" / ".gnupg" / f.getName
}

TaskKey[Unit]("check") := {
  val log = state.value.log
  log.info("running gpgAddKey task")
  val (fingerprint, keys) = Def.taskDyn {
    val fingerprint = gpgAddKey.value
    Def.task {
      val keys = gpgListKeys.value
      (fingerprint, keys)
    }
  }.value
  val secondSignSubKey = keys(1).subKeys(1)
  require(secondSignSubKey.keyLength === gpgKeyLength.value)
  require(secondSignSubKey.algorithm === 1)
  require(secondSignSubKey.capabilities === Set(Sign))
  // The duration is too inaccurate.
  //val duration = Duration.between(secondSignSubKey.expirationDate.get, secondSignSubKey.creationDate)
  //require(duration.compareTo(Duration.of(30, ChronoUnit.DAYS)) === 0)
  require(secondSignSubKey.fingerprint === fingerprint)
}

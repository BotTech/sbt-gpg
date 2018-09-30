import java.time.Instant

import nz.co.bottech.sbt.gpg._
import nz.co.bottech.sbt.gpg.GpgListingParser._
import nz.co.bottech.sbt.gpg.GpgListingParser.Capability._
import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgNameReal := "Jim Bob"
gpgNameEmail := "jim.bob@example.com"
gpgPassphrase := Some("oh no this shouldn't be here")

gpgParametersFile := {
  file("/") / "root" / ".gnupg" / gpgParametersFile.value.getName
}

TaskKey[Unit]("check") := {
  val log = state.value.log
  log.info("running gpgListKeys task")
  val keys = gpgListKeys.value
  require(keys.size === 2)
  val alice = GpgUserID(
    Validity.UlimatelyValid,
    Instant.ofEpochMilli(1538303044),
    "AD66BA4CCE5C5EE636FAD1BE2B9A0DB88903021E",
    "Alice <alice@example.com>",
    ""
  )
  val firstEncryptSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "068102BF24E8968E",
    Instant.ofEpochMilli(1538303044),
    None,
    Set(Encrypt),
    "",
    Set.empty,
    "",
    "",
    "C2B201C32C6050588456BBC9068102BF24E8968E",
    ""
  )
  val firstSignSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    3072,
    17,
    "8D1C95A18943856B",
    Instant.ofEpochMilli(1538303702),
    Some(Instant.ofEpochMilli(1569839702)),
    Set(Sign),
    "",
    Set.empty,
    "",
    "",
    "1F5B17A1D9EEB0F9221704A58D1C95A18943856B",
    ""
  )
  val firstKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "18D55882D01F35B2",
    Instant.ofEpochMilli(1538303044),
    None,
    "u",
    Set(
      Sign,
      Certify,
      PrimaryEncrypt,
      PrimarySign,
      PrimaryCertify
    ),
    "",
    Set.empty,
    "",
    "",
    "29ABDAD37A993917149E3B2A18D55882D01F35B2",
    "",
    alice,
    Seq(firstEncryptSubKey, firstSignSubKey)
  )
  require(keys(0) === firstKey)
  val bobette = GpgUserID(
    Validity.UlimatelyValid,
    Instant.ofEpochMilli(1538303779),
    "C595E1BBEAF34E53A50CC3A808C266A3DECA6A77",
    "Bobette <bob@gmail.com>",
    ""
  )
  val secondEncryptSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "EA4CB4E3E4DE6261",
    Instant.ofEpochMilli(1538303779),
    Some(Instant.ofEpochMilli(1569839779)),
    Set(Encrypt),
    "",
    Set.empty,
    "",
    "",
    "61C0F2973D7A4C8D6620A957EA4CB4E3E4DE6261",
    ""
  )
  val secondKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "70CEB1BC56395D7E",
    Instant.ofEpochMilli(1538303779),
    Some(Instant.ofEpochMilli(1569839779)),
    "u",
    Set(Sign, Certify, PrimaryEncrypt, PrimarySign, PrimaryCertify),
    "",
    Set.empty,
    "",
    "",
    "D152F3F412C7235D8240361970CEB1BC56395D7E",
    "",
    bobette,
    Seq(secondEncryptSubKey)
  )
  require(keys(1) === secondKey)
}

import java.time.Instant

import nz.co.bottech.sbt.gpg._
import nz.co.bottech.sbt.gpg.GpgListingParser._
import nz.co.bottech.sbt.gpg.GpgListingParser.Capability._
import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

TaskKey[Unit]("check") := {
  val keys = gpgListKeys.value
  require(keys.size === 2)
  val alice = GpgUserID(
    Validity.UlimatelyValid,
    Instant.ofEpochMilli(1538650227),
    "AD66BA4CCE5C5EE636FAD1BE2B9A0DB88903021E",
    "Alice <alice@example.com>",
    ""
  )
  val firstEncryptSubkey = GpgSubkeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "84F51E9BF1A32455",
    Instant.ofEpochMilli(1538650227),
    None,
    Set(Encrypt),
    "+",
    Set.empty,
    "",
    "",
    "30CB506F8E82BC16A172355284F51E9BF1A32455",
    "0206243F9D91A60F9DC6BF7F831C194BA7BF3747"
  )
  val firstSignSubkey = GpgSubkeyInfo(
    Validity.UlimatelyValid,
    3072,
    17,
    "45D39C04A5481116",
    Instant.ofEpochMilli(1538650424),
    Some(Instant.ofEpochMilli(1541242424)),
    Set(Sign),
    "+",
    Set.empty,
    "",
    "",
    "77F56521010ACE05AB9184A345D39C04A5481116",
    "4B30991110A918FAC9688D315BC3312CDB200163"
  )
  val firstKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "968E1E13982F6604",
    Instant.ofEpochMilli(1538650227),
    None,
    "u",
    Set(
      Sign,
      Certify,
      PrimaryEncrypt,
      PrimarySign,
      PrimaryCertify
    ),
    "#",
    Set.empty,
    "",
    "",
    "47FDA829C0E78FDADB7C4FB7968E1E13982F6604",
    "E8C0F666509ED6077747730B3E85D6F4A9786200",
    alice,
    Seq(firstEncryptSubkey, firstSignSubkey)
  )
  require(keys(0) === firstKey)
  val bobette = GpgUserID(
    Validity.UlimatelyValid,
    Instant.ofEpochMilli(1538650526),
    "C595E1BBEAF34E53A50CC3A808C266A3DECA6A77",
    "Bobette <bob@gmail.com>",
    ""
  )
  val secondEncryptSubkey = GpgSubkeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "D9590422EEA48955",
    Instant.ofEpochMilli(1538650526),
    Some(Instant.ofEpochMilli(1541242526)),
    Set(Encrypt),
    "+",
    Set.empty,
    "",
    "",
    "D280CAFCE49A316AB1E44F0FD9590422EEA48955",
    "8B2C23DB534D4400D16412D9E5703D59EF21F782"
  )
  val secondKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "D84FFA353EFAA96B",
    Instant.ofEpochMilli(1538650526),
    Some(Instant.ofEpochMilli(1541242526)),
    "u",
    Set(Sign, Certify, PrimaryEncrypt, PrimarySign, PrimaryCertify),
    "+",
    Set.empty,
    "",
    "",
    "298686B3B7CFE03D307F3D39D84FFA353EFAA96B",
    "515BE0F0CF13DAA3D66A0E25B78DBDA270D1110F",
    bobette,
    Seq(secondEncryptSubkey)
  )
  require(keys(1) === secondKey)
}

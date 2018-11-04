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
    Validity.Expired,
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
    Instant.parse("1970-01-18T20:09:22.133Z"),
    "93B3F05FA969DF602A1785691CF3C5CFA34CACBC",
    "Bobette <bob@example.com>",
    ""
  )
  // TODO: Change this back to an encrypt only subkey.
  val secondSignSubkey = GpgSubkeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "9298FC5F87A30256",
    Instant.parse("1970-01-18T20:09:22.133Z"),
    None,
    Set(Sign),
    "+",
    Set.empty,
    "",
    "",
    "8690765CB35CE00FAB53FDCB9298FC5F87A30256",
    "F5B806CFFBE103E6F9E02AD8FC2FFE17B2BAC758"
  )
  val secondKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "D79ABBF6BC015967",
    Instant.parse("1970-01-18T20:09:22.133Z"),
    None,
    "u",
    Set(Authentication, Encrypt, Sign, Certify, PrimaryAuthentication, PrimaryEncrypt, PrimarySign, PrimaryCertify),
    "+",
    Set.empty,
    "",
    "",
    "B1FD663F1B9A6F3F13012D85D79ABBF6BC015967",
    "D249B5960D332C923772A0A4F8F70F8136720246",
    bobette,
    Seq(secondSignSubkey)
  )
  require(keys(1) === secondKey)
}

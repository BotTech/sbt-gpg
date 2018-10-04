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
    Instant.ofEpochMilli(1538282651),
    "AD66BA4CCE5C5EE636FAD1BE2B9A0DB88903021E",
    "Alice <alice@example.com>",
    "0"
  )
  val firstSignSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "7102E744704372FE",
    Instant.ofEpochMilli(1538282651),
    None,
    Set(Sign),
    "+",
    Set(ComplianceFlag.devs),
    "",
    "",
    "66CA2BF946D1863DF30E98407102E744704372FE",
    "463D2E0B8F943B44F3F6A67C6620EC0295C65E16"
  )
  val firstEncryptSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    3072,
    16,
    "33A645DAB69BA652",
    Instant.ofEpochMilli(1538282750),
    Some(Instant.ofEpochMilli(1569818750)),
    Set(Encrypt),
    "+",
    Set.empty,
    "",
    "",
    "5F688D6A65D5596FFE890F9F33A645DAB69BA652",
    "F0EB7519E7F9195C95104768963E2E435CE47C31"
  )
  val firstKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "815E51E2D58B34ED",
    Instant.ofEpochMilli(1538282651),
    None,
    "u",
    Set(
      Encrypt,
      Sign,
      Certify,
      Authentication,
      PrimaryEncrypt,
      PrimarySign,
      PrimaryCertify,
      PrimaryAuthentication
    ),
    "#",
    Set(ComplianceFlag.devs),
    "0",
    "",
    "E6E48F840A1BCD2FA67F50E1815E51E2D58B34ED",
    "8F79C2B5B961C62D9F4FF38AEA18A71C5C9F640B",
    alice,
    Seq(firstSignSubKey, firstEncryptSubKey)
  )
  require(keys(0) === firstKey)
  val bobette = GpgUserID(
    Validity.UlimatelyValid,
    Instant.ofEpochMilli(1538282715),
    "C595E1BBEAF34E53A50CC3A808C266A3DECA6A77",
    "Bobette <bob@gmail.com>",
    "0"
  )
  val secondEncryptSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "218D8D63CC7E4C7D",
    Instant.ofEpochMilli(1538282715),
    Some(Instant.ofEpochMilli(1601354715)),
    Set(Encrypt),
    "+",
    Set(ComplianceFlag.devs),
    "",
    "",
    "5B422E6DF09F43EF9DA19A50218D8D63CC7E4C7D",
    "DD0F913FD60A81E0C3F709A89ADF744313716AF3"
  )
  val secondKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "709CF2B6FF067DEB",
    Instant.ofEpochMilli(1538282715),
    Some(Instant.ofEpochMilli(1601354715)),
    "u",
    Set(Sign, Certify, PrimaryEncrypt, PrimarySign, PrimaryCertify),
    "+",
    Set(ComplianceFlag.devs),
    "0",
    "",
    "3F43DA9CAB5977759FC2E555709CF2B6FF067DEB",
    "D97B525C9AB26AB5822CF0A3DE0ADE15C5670960",
    bobette,
    Seq(secondEncryptSubKey)
  )
  require(keys(1) === secondKey)
}

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
  val keys = gpgListKeys.value
  require(keys.size === 2)
  val alice = GpgUserID(
    Validity.UlimatelyValid,
    Instant.ofEpochMilli(1538299937),
    "AD66BA4CCE5C5EE636FAD1BE2B9A0DB88903021E",
    "Alice <alice@example.com>",
    ""
  )
  val firstEncryptSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "282A23DC16F896B9",
    Instant.ofEpochMilli(1538299937),
    None,
    Set(Encrypt),
    "+",
    Set.empty,
    "",
    "",
    "AE1EFB7C13015FDC250F1CD5282A23DC16F896B9",
    "7EAB267DBB845913ABFA2A6B806400F3E15CA5DD"
  )
  val firstSignSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    3072,
    17,
    "45CEDE1CDF3D80C6",
    Instant.ofEpochMilli(1538300077),
    Some(Instant.ofEpochMilli(1569836077)),
    Set(Sign),
    "+",
    Set.empty,
    "",
    "",
    "89B28FF77D2AD4D5A704896745CEDE1CDF3D80C6",
    "ADBEB85D025EC0853D799715663AE42D564C7917"
  )
  val firstKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    4096,
    1,
    "FE8BBF5E2D69BB71",
    Instant.ofEpochMilli(1538299937),
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
    "FAAF8CA0F77452712D468730FE8BBF5E2D69BB71",
    "12DE8B1311390B60FCB1B1FB34EF4831A0699059",
    alice,
    Seq(firstEncryptSubKey, firstSignSubKey)
  )
  require(keys(0) === firstKey)
  val bobette = GpgUserID(
    Validity.UlimatelyValid,
    Instant.ofEpochMilli(1538300341),
    "C595E1BBEAF34E53A50CC3A808C266A3DECA6A77",
    "Bobette <bob@gmail.com>",
    ""
  )
  val secondEncryptSubKey = GpgSubKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "94CC94C6B2059D2F",
    Instant.ofEpochMilli(1538300341),
    Some(Instant.ofEpochMilli(1569836341)),
    Set(Encrypt),
    "+",
    Set.empty,
    "",
    "",
    "426497E0A89864CD2B0E83B394CC94C6B2059D2F",
    "6B7C1DB6437C404FB64C9B5C77357229C63B80D2"
  )
  val secondKey = GpgKeyInfo(
    Validity.UlimatelyValid,
    3072,
    1,
    "289E3A8C38A903A0",
    Instant.ofEpochMilli(1538300341),
    Some(Instant.ofEpochMilli(1569836341)),
    "u",
    Set(Sign, Certify, PrimaryEncrypt, PrimarySign, PrimaryCertify),
    "+",
    Set.empty,
    "",
    "",
    "94828235C86AB40CC43BD07F289E3A8C38A903A0",
    "5C9689C82556D6F6F846A265A096F9DE7BCB1069",
    bobette,
    Seq(secondEncryptSubKey)
  )
  require(keys(1) === secondKey)
}

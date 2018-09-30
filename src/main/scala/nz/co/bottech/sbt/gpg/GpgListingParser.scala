package nz.co.bottech.sbt.gpg

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

import nz.co.bottech.sbt.gpg.GpgListingParser.Capability._
import nz.co.bottech.sbt.gpg.GpgListingParser.ComplianceFlag._
import nz.co.bottech.sbt.gpg.GpgListingParser.TypeOfRecord._
import nz.co.bottech.sbt.gpg.GpgListingParser.Validity._

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

// https://git.gnupg.org/cgi-bin/gitweb.cgi?p=gnupg.git;a=blob_plain;f=doc/DETAILS
object GpgListingParser {

  final case class GpgListingParseException(message: String, line: String) extends Exception(message)

  sealed class TypeOfRecord(protected val typ: String) {

    def unapply(arg: String): Boolean = arg == typ

    def unapply(listing: Listing): Option[Listing] = {
      Some(listing).filter(_.typeOfRecord.typ == typ)
    }
  }

  object TypeOfRecord {

    final case object PublicKey extends TypeOfRecord("pub")

    final case object Certificate extends TypeOfRecord("crt")

    final case object CertificateAndPrivateKey extends TypeOfRecord("crs")

    final case object Subkey extends TypeOfRecord("sub")

    final case object SecretSubkey extends TypeOfRecord("ssb")

    final case object UserID extends TypeOfRecord("uid")

    final case object UserAttribute extends TypeOfRecord("uat")

    final case object Signature extends TypeOfRecord("sig")

    final case object RevocationSignature extends TypeOfRecord("rev")

    final case object RevocationSignatureStandalone extends TypeOfRecord("rvs")

    final case object Fingerprint extends TypeOfRecord("fpr")

    final case object PublicKeyData extends TypeOfRecord("pkd")

    final case object Keygrip extends TypeOfRecord("grp")

    final case object RevocationKey extends TypeOfRecord("rvk")

    final case object TOFUStatistics extends TypeOfRecord("tfs")

    final case object TrustDatabaseInformation extends TypeOfRecord("tru")

    final case object SignatureSubpacket extends TypeOfRecord("spk")

    final case object ConfigurationData extends TypeOfRecord("cfg")

    final case class Other(override protected val typ: String) extends TypeOfRecord(typ)

  }

  sealed class Validity(validity: String) {

    def unapply(arg: String): Boolean = arg == validity
  }

  sealed trait SignatureValidity extends Validity

  object Validity {

    final case object Unknown extends Validity("o")

    final case object Invalid extends Validity("i")

    final case object Disabled extends Validity("d")

    final case object Revoked extends Validity("r")

    final case object Expired extends Validity("e")

    final case object NoValue extends Validity("-")

    final case object Undefined extends Validity("q")

    final case object NotValid extends Validity("n")

    final case object MarginalValid extends Validity("m")

    final case object FullyValid extends Validity("f")

    final case object UlimatelyValid extends Validity("u")

    final case object WellKnownPrivatePart extends Validity("w")

    final case object Special extends Validity("s")

    final case object SignatureGood extends Validity("!") with SignatureValidity

    final case object SignatureBad extends Validity("-") with SignatureValidity

    final case object NoPublicKey extends Validity("?") with SignatureValidity

    final case object OtherError extends Validity("%") with SignatureValidity

  }

  sealed class Capability(validity: Char) {

    def unapply(arg: Char): Boolean = arg == validity
  }

  object Capability {

    case object Encrypt extends Capability('e')

    case object Sign extends Capability('s')

    case object Certify extends Capability('c')

    case object Authentication extends Capability('a')

    case object PrimaryEncrypt extends Capability('E')

    case object PrimarySign extends Capability('S')

    case object PrimaryCertify extends Capability('C')

    case object PrimaryAuthentication extends Capability('A')

    case object PrimaryDisabled extends Capability('D')

  }

  sealed class ComplianceFlag(flags: String) {

    def unapply(arg: String): Boolean = arg == flags
  }

  object ComplianceFlag {

    case object RFC4880bis extends ComplianceFlag("8")

    case object devs extends ComplianceFlag("23")

    case object ROCA extends ComplianceFlag("6001")

  }

  final case class Listing(typeOfRecord: TypeOfRecord,
                           validity: Option[Validity],
                           length: Option[Int],
                           algorithm: Option[Int],
                           keyID: String,
                           creationDate: Option[Instant],
                           expirationDate: Option[Instant],
                           field8: String,
                           ownerTrust: String,
                           userID: String,
                           signatureClass: String,
                           capabilities: Set[Capability],
                           field13: String,
                           flagField: String,
                           tokenSerialNumber: String,
                           hashAlgorithm: String,
                           curveName: String,
                           complianceFlags: Set[ComplianceFlag],
                           lastUpdate: Option[Instant],
                           origin: String,
                           comment: String)

  def parseAll(lines: Seq[String]): Seq[Try[Listing]] = {
    lines.map(parse)
  }

  // TODO: Handle the special field formats.

  def parse(line: String): Try[Listing] = {
    val fields = line.split(":").toSeq
    fields match {
      case PublicKey() +: tail => parseRecordListing(PublicKey, tail, line)
      case Certificate() +: tail => parseRecordListing(Certificate, tail, line)
      case CertificateAndPrivateKey() +: tail => parseRecordListing(CertificateAndPrivateKey, tail, line)
      case Subkey() +: tail => parseRecordListing(Subkey, tail, line)
      case SecretSubkey() +: tail => parseRecordListing(SecretSubkey, tail, line)
      case UserID() +: tail => parseRecordListing(UserID, tail, line)
      case UserAttribute() +: tail => parseRecordListing(UserAttribute, tail, line)
      case Signature() +: tail => parseRecordListing(Signature, tail, line)
      case RevocationSignature() +: tail => parseRecordListing(RevocationSignature, tail, line)
      case RevocationSignatureStandalone() +: tail => parseRecordListing(RevocationSignatureStandalone, tail, line)
      case Fingerprint() +: tail => parseRecordListing(Fingerprint, tail, line)
      case PublicKeyData() +: tail => parseRecordListing(PublicKeyData, tail, line)
      case Keygrip() +: tail => parseRecordListing(Keygrip, tail, line)
      case RevocationKey() +: tail => parseRecordListing(RevocationKey, tail, line)
      case TOFUStatistics() +: tail => parseRecordListing(TOFUStatistics, tail, line)
      case TrustDatabaseInformation() +: tail => parseRecordListing(TrustDatabaseInformation, tail, line)
      case SignatureSubpacket() +: tail => parseRecordListing(SignatureSubpacket, tail, line)
      case ConfigurationData() +: tail => parseRecordListing(ConfigurationData, tail, line)
      case typ +: tail => parseRecordListing(Other(typ), tail, line)
      case Seq() => Failure(GpgListingParseException("Unknown record type", line))
    }
  }

  private def parseRecordListing(record: TypeOfRecord, fields: Seq[String], line: String): Try[Listing] = {
    val (validity, validityTail) = parseValidity(fields)
    for {
      (length, lengthTail) <- parseInt(validityTail)
      (algorithm, algorithmTail) <- parseInt(lengthTail)
      (keyID, keyIDTail) = parseString(algorithmTail)
      (creationDate, creationDateTail) <- parseInstant(keyIDTail)
      (expirationDate, expirationDateTail) <- parseInstant(creationDateTail)
      (field8, field8Tail) = parseString(expirationDateTail)
      (ownerTrust, ownerTrustTail) = parseString(field8Tail)
      (userID, userIDTail) = parseString(ownerTrustTail)
      (signatureClass, signatureClassTail) = parseString(userIDTail)
      (capabilities, capabilitiesTail) <- parseCapabilities(signatureClassTail, line)
      (field13, field13Tail) = parseString(capabilitiesTail)
      (flagField, flagFieldTail) = parseString(field13Tail)
      (tokenSerialNumber, tokenSerialNumberTail) = parseString(flagFieldTail)
      (hashAlgorithm, hashAlgorithmTail) = parseString(tokenSerialNumberTail)
      (curveName, curveNameTail) = parseString(hashAlgorithmTail)
      (complianceFlags, complianceFlagsTail) <- parseComplianceFlags(curveNameTail, line)
      (lastUpdate, lastUpdateTail) <- parseInstant(complianceFlagsTail)
      (origin, originTail) = parseString(lastUpdateTail)
      (comment, _) = parseString(originTail)
    } yield Listing(
      record,
      validity,
      length,
      algorithm,
      keyID,
      creationDate,
      expirationDate,
      field8,
      ownerTrust,
      userID,
      signatureClass,
      capabilities,
      field13,
      flagField,
      tokenSerialNumber,
      hashAlgorithm,
      curveName,
      complianceFlags,
      lastUpdate,
      origin,
      comment
    )
  }

  private def parseValidity(fields: Seq[String]): (Option[Validity], Seq[String]) = {
    fields match {
      case head +: tail =>
        val maybeValidity = head match {
          case Unknown() => Some(Unknown)
          case Invalid() => Some(Invalid)
          case Disabled() => Some(Disabled)
          case Revoked() => Some(Revoked)
          case Expired() => Some(Expired)
          case NoValue() => Some(NoValue)
          case Undefined() => Some(Undefined)
          case NotValid() => Some(NotValid)
          case MarginalValid() => Some(MarginalValid)
          case FullyValid() => Some(FullyValid)
          case UlimatelyValid() => Some(UlimatelyValid)
          case WellKnownPrivatePart() => Some(WellKnownPrivatePart)
          case Special() => Some(Special)
          case SignatureGood() => Some(SignatureGood)
          case SignatureBad() => Some(SignatureBad)
          case NoPublicKey() => Some(NoPublicKey)
          case OtherError() => Some(OtherError)
          case _ => None
        }
        (maybeValidity, tail)
      case Seq() => (None, Seq.empty)
    }
  }

  private def parseInt(fields: Seq[String]): Try[(Option[Int], Seq[String])] = {
    tryParseMaybe(fields, field => Try(field.toInt))
  }

  private def parseString(fields: Seq[String]): (String, Seq[String]) = {
    fields match {
      case head +: tail => (head, tail)
      case Seq() => ("", Seq.empty)
    }
  }

  private final val DateTimeFormat = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss")

  private def parseInstant(fields: Seq[String]): Try[(Option[Instant], Seq[String])] = {
    tryParseMaybe(
      fields, field => {
        Try(LocalDateTime.parse(field, DateTimeFormat).toInstant(ZoneOffset.UTC))
          .orElse(Try(Instant.ofEpochMilli(field.toLong)))
      })
  }

  private def tryParseMaybe[A](fields: Seq[String], f: String => Try[A]): Try[(Option[A], Seq[String])] = {
    fields match {
      case "" +: tail => Success(None, tail)
      case head +: tail => f(head).map(field => (Some(field), tail))
      case Seq() => Success((None, Seq()))
    }
  }

  private def parseCapabilities(fields: Seq[String], line: String): Try[(Set[Capability], Seq[String])] = {
    tryParseSet(fields, _.toSeq, parseCapability(_: Char, line))
  }

  private def parseCapability(char: Char, line: String): Try[Capability] = {
    char match {
      case Encrypt() => Success(Encrypt)
      case Sign() => Success(Sign)
      case Certify() => Success(Certify)
      case Authentication() => Success(Authentication)
      case PrimaryEncrypt() => Success(PrimaryEncrypt)
      case PrimarySign() => Success(PrimarySign)
      case PrimaryCertify() => Success(PrimaryCertify)
      case PrimaryAuthentication() => Success(PrimaryAuthentication)
      case PrimaryDisabled() => Success(PrimaryDisabled)
      case _ => Failure(GpgListingParseException(s"Unable to parse '$char' as a capability.", line))
    }
  }

  private def parseComplianceFlags(fields: Seq[String], line: String): Try[(Set[ComplianceFlag], Seq[String])] = {
    tryParseSet(fields, _.split(" "), parseComplianceFlag(_: String, line)).map {
      case (flags, remaining) => (flags.flatten, remaining)
    }
  }

  private def parseComplianceFlag(flag: String, line: String): Try[Option[ComplianceFlag]] = {
    flag match {
      case RFC4880bis() => Success(Some(RFC4880bis))
      case devs() => Success(Some(devs))
      case ROCA() => Success(Some(ROCA))
      case "" => Success(None)
      case _ => Failure(GpgListingParseException(s"Unable to parse '$flag' as a compliance flag.", line))
    }
  }

  private def tryParseSet[A, B](fields: Seq[String],
                                splitField: String => Seq[A],
                                tryParse: A => Try[B]): Try[(Set[B], Seq[String])] = {
    @tailrec
    def loop(field: Seq[A], acc: Set[B]): Try[Set[B]] = {
      field match {
        case x +: tail => tryParse(x) match {
          case Success(element) => loop(tail, acc + element)
          case Failure(ex) => Failure(ex)
        }
        case Seq() => Success(acc)
      }
    }

    fields match {
      case head +: tail => loop(splitField(head), Set.empty).map(x => (x, tail))
      case Seq() => Success((Set.empty, Seq()))
    }
  }
}

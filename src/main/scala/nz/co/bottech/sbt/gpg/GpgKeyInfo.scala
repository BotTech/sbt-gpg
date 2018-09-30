package nz.co.bottech.sbt.gpg

import java.time.Instant

import nz.co.bottech.sbt.gpg.GpgListingParser.TypeOfRecord._
import nz.co.bottech.sbt.gpg.GpgListingParser._
import sbt.util.Logger

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

final case class GpgKeyInfo(validity: Validity,
                            keyLength: Int,
                            algorithm: Int,
                            keyID: String,
                            creationDate: Instant,
                            expirationDate: Option[Instant],
                            ownerTrust: String,
                            capabilities: Set[Capability],
                            tokenSerialNumber: String,
                            complianceFlags: Set[ComplianceFlag],
                            origin: String,
                            comment: String,
                            fingerprint: String,
                            keyGrip: String,
                            userID: GpgUserID,
                            subKeys: Seq[GpgSubKeyInfo])

object GpgKeyInfo {

  final case class GpgMissingKeyInfo(message: String) extends Exception(message)

  def group(listings: Seq[Listing], log: Logger): Seq[Try[GpgKeyInfo]] = {
    @tailrec
    def loop(remaining: Seq[Listing], acc: Vector[Try[GpgKeyInfo]]): Seq[Try[GpgKeyInfo]] = {
      remaining match {
        case listing +: tail if listing.typeOfRecord == PublicKey =>
          val keyListings = KeyListings(listing, None, None, None, Vector.empty)
          val (keyInfo, nextRemaining) = groupPublicKey(tail, keyListings, log)
          loop(nextRemaining, acc :+ keyInfo)
        case listing +: tail =>
          log.debug(s"Skipping listing looking for public key: $listing")
          loop(tail, acc)
        case Seq() => acc
      }
    }

    loop(listings, Vector.empty)
  }

  private final case class KeyListings(pub: Listing,
                                       fpr: Option[Listing],
                                       grp: Option[Listing],
                                       uid: Option[Listing],
                                       subs: Vector[Try[GpgSubKeyInfo]]) {

    def keyInfo: Try[GpgKeyInfo] = {
      for {
        validity <- tryMaybeField(pub.validity, "Public key listing was missing the validity field.")
        keyLength <- tryMaybeField(pub.length, "Public key listing was missing the key length field.")
        algorithm <- tryMaybeField(pub.algorithm, "Public key listing was missing the algorithm field.")
        keyID <- tryStringField(pub.keyID, "Public key listing was missing the key ID field.")
        creationDate <- tryMaybeField(pub.creationDate, "Public key listing was missing the creation date field.")
        ownerTrust <- tryStringField(pub.ownerTrust, "Public key listing was missing the owner trust field.")
        fprListing <- tryMaybeField(fpr, "Fingerprint listing was missing.")
        fingerprint <- tryStringField(fprListing.userID, "Fingerprint listing was missing the user ID field.")
        keyGrip <- tryKeyGrip(grp)
        userID <- tryUserID(uid)
        subKeys <- trySubKeys(subs)
      } yield GpgKeyInfo(
        validity,
        keyLength,
        algorithm,
        keyID,
        creationDate,
        pub.expirationDate,
        ownerTrust,
        pub.capabilities,
        pub.tokenSerialNumber,
        pub.complianceFlags,
        pub.origin,
        pub.comment,
        fingerprint,
        keyGrip,
        userID,
        subKeys
      )
    }

    private def trySubKeys(subKeys: Seq[Try[GpgSubKeyInfo]]): Try[Seq[GpgSubKeyInfo]] = {
      subKeys.foldLeft[Try[Seq[GpgSubKeyInfo]]](Success(Vector.empty)) {
        case (Success(acc), Success(subKey)) => Success(acc :+ subKey)
        case (_, Failure(ex)) => Failure(ex)
        case (Failure(ex), _) => Failure(ex)
      }
    }

    private def tryUserID(maybeUID: Option[Listing]): Try[GpgUserID] = {
      for {
        uid <- tryMaybeField(maybeUID, "User ID listing was missing.")
        validity <- tryMaybeField(uid.validity, "User ID listing was missing the validity field.")
        creationDate <- tryMaybeField(uid.creationDate, "User ID listing was missing the creation date field.")
        hash <- tryStringField(uid.field8, "User ID listing was missing the UID hash field.")
        userID <- tryStringField(uid.userID, "User ID listing was missing the user ID field.")
      } yield GpgUserID(
        validity,
        creationDate,
        hash,
        userID,
        uid.origin
      )
    }
  }

  private final case class SubKeyListings(sub: Listing, fpr: Option[Listing], grp: Option[Listing]) {

    def subKeyInfo: Try[GpgSubKeyInfo] = {
      for {
        validity <- tryMaybeField(sub.validity, "Subkey listing was missing the validity field.")
        keyLength <- tryMaybeField(sub.length, "Subkey listing was missing the key length field.")
        algorithm <- tryMaybeField(sub.algorithm, "Subkey listing was missing the algorithm field.")
        keyID <- tryStringField(sub.keyID, "Subkey listing was missing the key ID field.")
        creationDate <- tryMaybeField(sub.creationDate, "Subkey listing was missing the creation date field.")
        fprListing <- tryMaybeField(fpr, "Fingerprint listing was missing.")
        fingerprint <- tryStringField(fprListing.userID, "Fingerprint listing was missing the user ID field.")
        keyGrip <- tryKeyGrip(grp)
      } yield GpgSubKeyInfo(
        validity,
        keyLength,
        algorithm,
        keyID,
        creationDate,
        sub.expirationDate,
        sub.capabilities,
        sub.tokenSerialNumber,
        sub.complianceFlags,
        sub.origin,
        sub.comment,
        fingerprint,
        keyGrip
      )
    }
  }

  private def tryMaybeField[A](maybeField: Option[A], message: String) = {
    maybeField.fold[Try[A]](Failure(GpgMissingKeyInfo(message)))(Success(_))
  }

  private def tryStringField[A](field: String, message: String) = {
    val trimmed = field.trim
    if (trimmed.isEmpty) {
      Failure(GpgMissingKeyInfo(message))
    } else {
      Success(trimmed)
    }
  }

  private def tryKeyGrip(grp: Option[Listing]): Try[String] = {
    grp.map { grpListing =>
      tryStringField(grpListing.userID, "Keygrip listing was missing the user ID field.")
    }.getOrElse(Success(""))
  }

  private def groupPublicKey(listings: Seq[Listing],
                             keyListings: KeyListings,
                             log: Logger): (Try[GpgKeyInfo], Seq[Listing]) = {
    @tailrec
    def loop(remaining: Seq[Listing], acc: KeyListings): (Try[GpgKeyInfo], Seq[Listing]) = {
      remaining match {
        case Fingerprint(listing) +: tail =>
          val nextAcc = acc.copy(fpr = Some(addListing(listing, acc.fpr, log, "fingerprint")))
          loop(tail, nextAcc)
        case Keygrip(listing) +: tail =>
          val nextAcc = acc.copy(grp = Some(addListing(listing, acc.grp, log, "keygrip")))
          loop(tail, nextAcc)
        case UserID(listing) +: tail =>
          val nextAcc = acc.copy(uid = Some(addListing(listing, acc.uid, log, "user id")))
          loop(tail, nextAcc)
        case Subkey(listing) +: tail =>
          val subKeyListings = SubKeyListings(listing, None, None)
          val (trySubKey, nextRemaining) = groupSubKey(tail, subKeyListings, log)
          val nextAcc = acc.copy(subs = acc.subs :+ trySubKey)
          loop(nextRemaining, nextAcc)
        case PublicKey(_) +: _ => (acc.keyInfo, remaining)
        case listing +: tail =>
          log.debug(s"Skipping listing as it is not part of the key info: $listing")
          loop(tail, acc)
        case Seq() => (acc.keyInfo, Seq())
      }
    }

    loop(listings, keyListings)
  }

  private def groupSubKey(listings: Seq[Listing],
                          keyListings: SubKeyListings,
                          log: Logger): (Try[GpgSubKeyInfo], Seq[Listing]) = {
    @tailrec
    def loop(remaining: Seq[Listing], acc: SubKeyListings): (Try[GpgSubKeyInfo], Seq[Listing]) = {
      remaining match {
        case Fingerprint(listing) +: tail =>
          val nextAcc = acc.copy(fpr = Some(addListing(listing, acc.fpr, log, "fingerprint")))
          loop(tail, nextAcc)
        case Keygrip(listing) +: tail =>
          val nextAcc = acc.copy(grp = Some(addListing(listing, acc.grp, log, "keygrip")))
          loop(tail, nextAcc)
        case _ => (acc.subKeyInfo, remaining)
      }
    }

    loop(listings, keyListings)
  }

  private def addListing(listing: Listing, maybeListing: Option[Listing], log: Logger, listingName: String) = {
    maybeListing.foreach { _ =>
      log.debug(s"Skipping $listingName listing as it has already been found: $listing")
    }
    maybeListing.getOrElse(listing)
  }
}

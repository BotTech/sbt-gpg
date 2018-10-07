package nz.co.bottech.sbt.gpg

import java.time.Instant

import nz.co.bottech.sbt.gpg.GpgListingParser.{Capability, ComplianceFlag, Validity}

final case class GpgSubkeyInfo(validity: Validity,
                               keyLength: Int,
                               algorithm: Int,
                               keyID: String,
                               creationDate: Instant,
                               expirationDate: Option[Instant],
                               capabilities: Set[Capability],
                               tokenSerialNumber: String,
                               complianceFlags: Set[ComplianceFlag],
                               origin: String,
                               comment: String,
                               fingerprint: String,
                               keyGrip: String)

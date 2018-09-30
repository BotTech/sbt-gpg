package nz.co.bottech.sbt.gpg

import java.time.Instant

import nz.co.bottech.sbt.gpg.GpgListingParser.Validity

final case class GpgUserID(validity: Validity,
                           creationDate: Instant,
                           hash: String,
                           userID: String,
                           origin: String)

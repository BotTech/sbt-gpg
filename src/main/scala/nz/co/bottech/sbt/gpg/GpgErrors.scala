package nz.co.bottech.sbt.gpg

object GpgErrors {

  final case class GpgFailedToExecute(message: String) extends Exception(message)

  final case class GpgUnknownVersionException(message: String) extends Exception(message)

  final case class GpgCannotParseOutput(message: String) extends Exception(message)

  final case class GpgMissingSecretKey(message: String) extends Exception(message)

  final case class GpgErrorChangingPassphrase(message: String) extends Exception(message)

}

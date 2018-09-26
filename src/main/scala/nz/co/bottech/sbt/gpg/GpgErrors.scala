package nz.co.bottech.sbt.gpg

object GpgErrors {

  final case class GpgUnknownVersionException(message: String) extends Exception(message)

  final case class GpgCannotParseOutput(message: String) extends Exception(message)

}

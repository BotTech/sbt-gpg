package nz.co.bottech.sbt

final case class GpgCommandFailedException(command: String, exitCode: Int)
  extends Exception(s"Failed to execute gpg command '$command' (exit code $exitCode).")

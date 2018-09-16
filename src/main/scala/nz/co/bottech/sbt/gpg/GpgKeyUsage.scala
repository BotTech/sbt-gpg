package nz.co.bottech.sbt.gpg

sealed trait GpgKeyUsage

object GpgKeyUsage {

  final case object encrypt extends GpgKeyUsage

  final case object sign extends GpgKeyUsage

  final case object auth extends GpgKeyUsage

  final case object cert extends GpgKeyUsage

}

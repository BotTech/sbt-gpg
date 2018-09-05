package nz.co.bottech.sbt.gpg

sealed trait GpgVersion

case object GpgVersion2Dot0 extends GpgVersion

case object GpgVersion2Dot1 extends GpgVersion

case object GpgVersion2Dot2 extends GpgVersion

package nz.co.bottech.sbt.gpg

sealed trait GpgVersion

object GpgVersion {

  def commands(version: GpgVersion): BaseGpgCommands = version match {
    case GpgVersion2Dot0 => v2_0.GpgCommands
    case GpgVersion2Dot1 => v2_1.GpgCommands
    case GpgVersion2Dot2 => v2_2.GpgCommands
  }
}

case object GpgVersion2Dot0 extends GpgVersion {

  override def toString: String = "2.0"
}

case object GpgVersion2Dot1 extends GpgVersion {

  override def toString: String = "2.1"
}

case object GpgVersion2Dot2 extends GpgVersion {

  override def toString: String = "2.2"
}

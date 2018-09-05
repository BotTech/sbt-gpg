package nz.co.bottech.sbt.gpg

sealed trait GpgArgument {

  def toOptions: Seq[String]
}

final case class GpgFlag(flag: String) extends GpgArgument {

  override def toOptions: Seq[String] = Seq(flag)
}

object GpgFlag {

  val batch = GpgFlag("--batch")
  val withColon = GpgFlag("--with-colons")
}

final case class GpgOptionValue(option: String, value: String) extends GpgArgument {

  override def toOptions: Seq[String] = Seq(option, value)
}

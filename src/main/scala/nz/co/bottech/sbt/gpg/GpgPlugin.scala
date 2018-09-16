package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgSettings._
import sbt._

import scala.language.implicitConversions

object GpgPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = Plugins.empty

  object autoImport extends GpgKeys {

    implicit def stringFlag(flag: String): GpgFlag = GpgFlag(flag)
  }

  override lazy val projectSettings: Seq[Def.Setting[_]] = rawSettings
}

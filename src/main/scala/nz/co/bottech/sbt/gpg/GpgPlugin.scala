package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgSettings._
import sbt._
import sbt.plugins.IvyPlugin

import scala.language.implicitConversions

object GpgPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = IvyPlugin

  object autoImport extends GpgKeys {

    implicit def stringFlag(flag: String): GpgFlag = GpgFlag(flag)
  }

  override lazy val projectSettings: Seq[Def.Setting[_]] = rawSettings
}

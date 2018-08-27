package nz.co.bottech.sbt

import nz.co.bottech.sbt.GpgSettings._
import sbt.{Def, _}

object GpgPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = Plugins.empty

  object autoImport extends GpgKeys

  override lazy val projectSettings: Seq[Def.Setting[_]] = rawSettings
}

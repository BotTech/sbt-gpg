package sbt

// TODO: Submit a PR to get this into sbt.
object ScriptedSettings {

  val rawSettings: Seq[Def.Setting[_]] = ScriptedPlugin.projectSettings

  def scriptedTask: Def.Initialize[InputTask[Unit]] = ScriptedPlugin.scriptedTask

}

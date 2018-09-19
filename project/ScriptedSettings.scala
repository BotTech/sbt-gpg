package sbt

import sbt.internal.util.complete.Parser
import ScriptedPlugin.autoImport._

// TODO: Submit a PR to get this into sbt.
object ScriptedSettings {

  val rawSettings: Seq[Def.Setting[_]] = ScriptedPlugin.projectSettings

  def scriptedParser(scriptedBase: File): Parser[Seq[String]] = ScriptedPlugin.scriptedParser(scriptedBase)

  def scriptedTaskWithParser(parser: File => Parser[Seq[String]]): Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val args = parser(sbtTestDirectory.value).parsed
    scriptedDependencies.value
    try {
      val method = scriptedRun.value
      val scriptedInstance = scriptedTests.value
      val dir = sbtTestDirectory.value
      val log = Boolean box scriptedBufferLog.value
      val launcher = sbtLauncher.value
      val opts = scriptedLaunchOpts.value.toArray
      val empty = new java.util.ArrayList[File]()
      val instances = Int box scriptedParallelInstances.value

      if (scriptedBatchExecution.value)
        method.invoke(scriptedInstance, dir, log, args.toArray, launcher, opts, empty, instances)
      else method.invoke(scriptedInstance, dir, log, args.toArray, launcher, opts, empty)
      ()
    } catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
  }
}

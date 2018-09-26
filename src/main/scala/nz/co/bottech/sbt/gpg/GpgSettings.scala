package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import nz.co.bottech.sbt.gpg.GpgTasks._
import sbt.Keys._
import sbt.{Def, _}

object GpgSettings {

  val rawSettings: Seq[Def.Setting[_]] = Seq(
    gpgAdditionalOptions := Seq.empty,
    gpgArguments := gpgArgumentsTask.value,
    gpgCommand := gpgCommandAndVersion.value._1,
    gpgCommandAndVersion := gpgCommandAndVersionTask.value,
    gpgExpireDate := "0",
    gpgHomeDir := None,
    gpgKeyLength := 4096,
    gpgKeyType := "RSA",
    gpgKeyUsage := Set(),
    gpgNameReal := "",
    gpgNameEmail := "",
    gpgParametersFile := gpgParametersFileTask.value,
    gpgPassphrase := None,
    gpgSelectPassphrase := gpgSelectPassphraseTask.value,
    gpgStatusFileDescriptor := 1,
    gpgSubkeyLength := gpgKeyLength.value,
    gpgSubkeyType := "RSA",
    gpgSubkeyUsage := Set(GpgKeyUsage.sign),
    gpgVersion := gpgCommandAndVersion.value._2
  ) ++
    inTaskRef(gpgGenerateKey)(
      Seq(gpgGenerateKey := generateKeyTask.value)
    ) ++
    inTask(gpgGenerateKey)(
      Seq(gpgParameters := Seq(gpgParametersFile.value.getPath))
    )

  def inTaskRef(t: Scoped)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    inScopeRef(ThisScope.copy(task = Select(t.key)))(ss)
  }

  def inScopeRef(scope: Scope)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    Project.transformRef(Scope.replaceThis(scope), ss)
  }
}

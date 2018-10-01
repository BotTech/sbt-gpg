package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import nz.co.bottech.sbt.gpg.GpgTasks._
import sbt._

object GpgSettings {

  val rawSettings: Seq[Def.Setting[_]] = Seq(
    gpgAdditionalOptions := Seq.empty,
    gpgArguments := gpgArgumentsTask.value,
    gpgCommand := gpgCommandAndVersion.value._1,
    gpgCommandAndVersion := gpgCommandAndVersionTask.value,
    gpgExpireDate := "0",
    gpgHomeDir := None,
    gpgKeyFingerprint := None,
    gpgKeyLength := 4096,
    gpgKeyType := "RSA",
    gpgKeyUsage := Set(),
    gpgNameReal := "",
    gpgNameEmail := "",
    gpgParameters := Seq.empty,
    gpgParametersFile := gpgParametersFileTask.value,
    gpgPassphrase := None,
    gpgPassphraseFile := gpgPassphraseFileTask.value,
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
    ) ++
    inTaskRef(gpgListKeys)(
      Seq(gpgListKeys := listKeysTask.value)
    ) ++
    inTaskRef(gpgAddKey)(
      Seq(gpgAddKey := addKeyTask.value)
    ) ++
    inTask(gpgAddKey)(
      Seq(
        gpgArguments := gpgArguments.value ++ Seq(
          GpgOption.pinentryMode("loopback"),
          GpgOption.passphraseFile(gpgPassphraseFile.value)
        ),
        gpgParameters := addKeyParametersTask.value
      )
    )

  def inTaskRef(t: Scoped)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    inScopeRef(ThisScope.copy(task = Select(t.key)))(ss)
  }

  def inScopeRef(scope: Scope)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    Project.transformRef(Scope.replaceThis(scope), ss)
  }
}

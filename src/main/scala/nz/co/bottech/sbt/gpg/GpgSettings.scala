package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import nz.co.bottech.sbt.gpg.GpgTasks._
import sbt._
import sbt.Keys._

object GpgSettings {

  val rawSettings: Seq[Def.Setting[_]] = Seq(
    gpgAdditionalOptions := Seq.empty,
    gpgArguments := gpgArgumentsTask.value,
    gpgArmor := true,
    gpgCommand := gpgCommandAndVersion.value._1,
    gpgCommandAndVersion := gpgCommandAndVersionTask.value,
    gpgExpireDate := "0",
    gpgHomeDir := None,
    gpgKeyFile := target.value / ".gnupg" / "key.asc",
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
        gpgArguments := passphraseArgumentsTask.value ++ gpgArguments.value,
        gpgParameters := addKeyParametersTask.value
      )
    ) ++
    inTaskRef(gpgExportSubKey)(
      Seq(gpgExportSubKey := exportSubKeyTask.value)
    ) ++
    inTask(gpgExportSubKey)(
      Seq(
        gpgArguments := exportArgumentsTask.value,
        gpgParameters := gpgKeyFingerprint.value.map(fpr => s"$fpr!").toSeq
      )
    ) ++
    inTaskRef(gpgImportKey)(
      Seq(gpgImportKey := importKeyTask.value)
    ) ++
    inTask(gpgImportKey)(
      Seq(gpgParameters := Seq(gpgKeyFile.value.getPath))
    )

  def inTaskRef(t: Scoped)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    inScopeRef(ThisScope.copy(task = Select(t.key)))(ss)
  }

  def inScopeRef(scope: Scope)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    Project.transformRef(Scope.replaceThis(scope), ss)
  }
}

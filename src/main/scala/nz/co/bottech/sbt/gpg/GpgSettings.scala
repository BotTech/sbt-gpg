package nz.co.bottech.sbt.gpg

import nz.co.bottech.sbt.gpg.GpgKeys._
import nz.co.bottech.sbt.gpg.GpgTasks._
import sbt.{Def, _}
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
        gpgParameters := Seq(mandatoryTask(gpgKeyFingerprint).value + "!")
      )
    ) ++
    inTaskRef(gpgImportKey)(
      Seq(gpgImportKey := importKeyTask.value)
    ) ++
    inTask(gpgImportKey)(
      Seq(gpgParameters := Seq(gpgKeyFile.value.getPath))
    ) ++
    inTaskRef(gpgSign)(
      Seq(gpgSign := signTask.value)
    ) ++
    inTask(gpgSign)(
      Seq(
        gpgArguments := signArgumentsTask.value,
        gpgParameters := Seq(mandatoryTask(gpgMessage).value.getPath)
      )
    )

  def inTaskRef(t: Scoped)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    inScopeRef(ThisScope.copy(task = Select(t.key)))(ss)
  }

  def inScopeRef(scope: Scope)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    Project.transformRef(Scope.replaceThis(scope), ss)
  }

  def mandatorySetting[A](setting: SettingKey[A]) = {
    setting ?? (throw new RuntimeException(s"${setting.key.label} must be set."))
  }

  def mandatoryTask[A](task: TaskKey[A]): Def.Initialize[Task[A]] = {
    task ?? (throw new RuntimeException(s"${task.key.label} must be set."))
  }
}

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
    gpgKeyLength := 4096,
    gpgKeyType := "RSA",
    gpgKeyUsage := Set(),
    gpgKeyFile := target.value / ".gnupg" / "key.asc",
    gpgParameters := Seq.empty,
    gpgPassphrase := None,
    gpgPassphraseFile := gpgPassphraseFileTask.value,
    gpgResolverName := resolverNameTask.value,
    gpgSelectPassphrase := gpgSelectPassphraseTask.value,
    gpgSignArtifacts := true,
    gpgSignedArtifacts := signedArtifactsTask.value,
    gpgStatusFileDescriptor := 1,
    gpgSubkeyLength := gpgKeyLength.value,
    gpgSubkeyType := gpgKeyType.value,
    gpgSubkeyUsage := Set(GpgKeyUsage.sign),
    gpgTrustFile := target.value / ".gnupg" / "trust.asc",
    gpgVersion := gpgCommandAndVersion.value._2,
    publishConfiguration := (publish / publishConfiguration).value,
    publishLocalConfiguration := (publishLocal / publishLocalConfiguration).value
  ) ++
    inTaskRef(gpgGenerateKey)(
      Seq(gpgGenerateKey := generateKeyTask.value)
    ) ++
    inTask(gpgGenerateKey)(
      Seq(
        gpgParameters := Seq(gpgParametersFile.value.getPath),
        gpgSelectPassphrase := None,
        gpgParametersFile := gpgParametersFileTask.value
      )
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
    inTaskRef(gpgExportKey)(
      Seq(gpgExportKey := exportKeyTask.value)
    ) ++
    inTask(gpgExportKey)(
      Seq(
        gpgArguments := exportArgumentsTask.value,
        gpgParameters := Seq(mandatoryTask(gpgKeyFingerprint).value)
      )
    ) ++
    inTaskRef(gpgExportSubkey)(
      Seq(gpgExportSubkey := exportSubkeyTask.value)
    ) ++
    inTask(gpgExportSubkey)(
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
        gpgParameters := Seq(mandatoryTask(gpgMessage).value.getPath),
        gpgSignatureFile := signatureFileTask.value
      )
    ) ++
    inTaskRef(gpgSigner)(
      Seq(gpgSigner := signerTask.value)
    ) ++
    inTask(publish)(
      Seq(
        gpgSignedArtifacts := signedArtifactsTask.value,
        publishConfiguration := publishConfigurationTask.value
      )
    ) ++
    inTask(publishLocal)(
      Seq(
        gpgSignArtifacts := false,
        publishMavenStyle := false,
        gpgResolverName := "local",
        gpgSignedArtifacts := signedArtifactsTask.value,
        publishLocalConfiguration := publishConfigurationTask.value
      )
    ) ++
    inTask(publishM2)(
      Seq(
        publishMavenStyle := true,
        gpgResolverName := Resolver.publishMavenLocal.name,
        gpgSignedArtifacts := signedArtifactsTask.value,
        publishM2Configuration := publishConfigurationTask.value
      )
    ) ++
    inTaskRef(gpgChangeKeyPassphrase)(
      Seq(gpgChangeKeyPassphrase := changeKeyPassphraseTask.value)
    ) ++
    inTask(gpgChangeKeyPassphrase)(
      Seq(
        gpgHomeDir := Some(tempTargetDirTask.value),
        gpgParameters := changePassphraseParametersTask.value
      )
    ) ++
    inTaskRef(gpgChangeSubkeyPassphrase)(
      Seq(gpgChangeSubkeyPassphrase := changeSubkeyPassphraseTask.value)
    ) ++
    inTask(gpgChangeSubkeyPassphrase)(
      Seq(
        gpgHomeDir := Some(tempTargetDirTask.value),
        gpgParameters := changePassphraseParametersTask.value
      )
    ) ++
    inTaskRef(gpgTrustKey)(
      Seq(gpgTrustKey := trustKeyTask.value)
    ) ++
    inTask(gpgTrustKey)(
      Seq(
        gpgArguments := gpgArguments.value ++ commandArgumentsTask.value,
        gpgCommands := trustKeyCommandsTask.value,
        gpgCommandFile := commandFileTask.value,
        gpgParameters := trustKeyParametersTask.value,
        gpgTrustLevel := 5
      )
    ) ++
    inTaskRef(gpgExportTrust)(
      Seq(gpgExportTrust := exportTrustTask.value)
    )

  def inTaskRef(t: Scoped)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    inScopeRef(ThisScope.copy(task = Select(t.key)))(ss)
  }

  def inScopeRef(scope: Scope)(ss: Seq[Setting[_]]): Seq[Setting[_]] = {
    Project.transformRef(Scope.replaceThis(scope), ss)
  }

  def mandatoryTask[A](task: TaskKey[A]): Def.Initialize[Task[A]] = {
    task ?? (throw new RuntimeException(s"${task.key.label} must be set."))
  }
}

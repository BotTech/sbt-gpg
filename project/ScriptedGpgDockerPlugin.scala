import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt.{Def, _}
import sbtdocker.DockerKeys._
import sbtdocker.immutable.Dockerfile
import sbtdocker.{DockerPlugin, DockerSettings, ImageName}

object ScriptedGpgDockerPlugin extends AutoPlugin {

  private val V2_0 = config("v2_0").extend(Compile).hide
  private val V2_1 = config("v2_1").extend(Compile).hide
  private val V2_2 = config("v2_2").extend(Compile).hide

  private final case class GpgVersionSettings(imageTag: String, gpgPackage: String, gpgCommand: String, gpgPropName: String)

  private val versionedSettings = Map(
    V2_0 -> GpgVersionSettings("trusty", "gnupg2", "gpg2", "gpg.command.v2_0"),
    V2_1 -> GpgVersionSettings("xenial", "gnupg2", "gpg2", "gpg.command.v2_1"),
    V2_2 -> GpgVersionSettings("bionic", "gnupg", "gpg", "gpg.command.v2_2")
  )

  override def requires: Plugins = DockerPlugin && ScriptedPlugin

  override def projectSettings: Seq[Def.Setting[_]] = {
    versionedSettings.flatMap((gpgSettings _).tupled).toSeq
  }

  private def gpgSettings(conf: Configuration, settings: GpgVersionSettings) = {
    inConfig(conf) {
      DockerSettings.baseDockerSettings ++
        ScriptedSettings.rawSettings ++
        versionSpecificSettings(settings)
    }
  }

  private def versionSpecificSettings(settings: GpgVersionSettings) = {
    import settings.{gpgCommand, gpgPackage, gpgPropName, imageTag}
    Seq(
      docker / dockerfile := gpgDockerfileSetting(imageTag, gpgPackage).value,
      docker / imageNames := dockerImageConfigNamesTask(imageTag).value,
      scripted := scriptedConfigTask.evaluated,
      scriptedBufferLog := false,
      scriptedDependencies := {
        scriptedDependencies.value
        docker.value
      },
      scriptedLaunchOpts := {
        gpgCommandPropSetting(imageTag, gpgCommand, gpgPropName).value ++
          scriptedLaunchOpts.value
      }
    )
  }

  private def gpgDockerfileSetting(ubuntuTag: String, gpgPackage: String) = Def.setting {
    Dockerfile.empty
      .from(s"ubuntu:$ubuntuTag")
      .run("apt-get", "update")
      .run("apt-get", "install", "-y", gpgPackage)
      .add(proxyScript.value, "/usr/local/bin/proxy")
      .run("chmod", "+x", "/usr/local/bin/proxy")
  }

  private def proxyScript = Def.setting {
    val conf = configuration.value.name
    sourceDirectory.value / "sbt-resources" / conf / "proxy"
  }

  private def dockerImageConfigNamesTask(ubuntuTag: String) = Def.task {
    val conf = configuration.value.name
    val tags = Seq(conf, ubuntuTag)
    val names = (docker / imageNames).value
    names.flatMap { name =>
      tags.map { tag =>
        name.copy(tag = Some(tag))
      }
    }
  }

  private def scriptedConfigTask = Def.inputTaskDyn[Unit] {
    val conf = configuration.value.name
    ScriptedSettings.scriptedTask.toTask(s" $conf/*")
  }

  private def gpgCommandPropSetting(imageTag: String, gpgCommand: String, gpgPropName: String) = Def.setting {
    val organisation = Some(organization.value)
    val name = Keys.normalizedName.value
    val imageName = ImageName(namespace = organisation, repository = name)
    val commandProp =
      s"""-D$gpgPropName=docker run --rm --mount type=bind,source=$$(pwd),target=/root $imageName:$imageTag proxy $gpgCommand"""
    val hiddenProps = hideOtherCommandProps(gpgPropName)
    commandProp +: hiddenProps
  }

  private def hideOtherCommandProps(gpgPropName: String) = {
    versionedSettings.values
      .map(_.gpgPropName)
      .filter(_ != gpgPropName)
      .map(name => s"""-D$name=echo skipping $name""")
      .toSeq
  }
}

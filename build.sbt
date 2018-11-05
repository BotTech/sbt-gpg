name := "sbt-gpg"
description := """An sbt plugin to sign artifacts using the GNU Privacy Guard."""
organization := "nz.co.bottech"
organizationName := "BotTech"
homepage := Some(url("https://github.com/BotTech/sbt-gpg"))
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

scalaVersion := "2.12.6"

sbtPlugin := true

libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.4" % Test
testFrameworks += new TestFramework("utest.runner.Framework")

enablePlugins(ScriptedGpgDockerPlugin)

scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  "-Dplugin.version=" + version.value
)

publishMavenStyle := false

bintrayOrganization := Some("bottech")
bintrayPackageLabels := Seq("sbt", "plugin", "gpg")

ghreleaseRepoOrg := organizationName.value

publishLocal / gpgSignArtifacts := false
gpgPassphrase := Option(System.getenv("PGP_PASS"))
gpgKeyFile := file("travis") / "key.asc"
gpgKeyFingerprint := "default"

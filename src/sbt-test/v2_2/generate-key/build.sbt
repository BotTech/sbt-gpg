scalaVersion := "2.12.6"

TaskKey[Unit]("check") := {
  gpgGenerateKey.value
  // TODO: Verify the output
}

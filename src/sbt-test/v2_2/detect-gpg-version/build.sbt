import nz.co.bottech.sbt.gpg._
import org.scalactic.Requirements._
import org.scalactic.TripleEquals._

scalaVersion := "2.12.6"

TaskKey[Unit]("check") := {
  val log = state.value.log
  log.info("checking detected gpg version")
  require(gpgVersion.value === GpgVersion2Dot2)
}

import org.scalactic.TripleEquals._
import org.scalactic.Requirements._

scalaVersion := "2.12.6"

gpgPassphrase := Some("password123")

TaskKey[Unit]("check") := {
  val trustFile = gpgTrustFile.value
  val lines = sbt.IO.readLines(trustFile)
  val trust = lines.filterNot(_.startsWith("#"))
  val expected = Seq(
    "E6E48F840A1BCD2FA67F50E1815E51E2D58B34ED:6:",
    "3F43DA9CAB5977759FC2E555709CF2B6FF067DEB:6:"
  )
  require(trust === expected)
}

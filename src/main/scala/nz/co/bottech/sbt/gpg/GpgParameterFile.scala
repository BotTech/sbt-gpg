package nz.co.bottech.sbt.gpg

import java.io.File

import sbt.util.Logger

final case class GpgKeyParameters(length: Int, typ: String, usage: Set[GpgKeyUsage])

final case class GpgName(real: String, email: String)

final case class GpgParameters(key: GpgKeyParameters,
                               subkey: GpgKeyParameters,
                               expire: String,
                               name: GpgName,
                               passphrase: Option[String])

object GpgParameterFile {

  def create(parameters: GpgParameters, file: File, log: Logger): File = {
    import parameters._
    val lines = {
      keyLines(key, "Key") ++:
        keyLines(subkey, "Subkey") ++:
        s"Expire-Date: $expire" +:
        nameLines(name) ++:
        passphrase.map(p => s"Passphrase: $p").toSeq
    }
    val parametersFile = IO.createTempFile(file, lines)
    log.debug(s"Parameters file written to $parametersFile.")
    parametersFile
  }

  private def keyLines(key: GpgKeyParameters, prefix: String): Seq[String] = {
    val lines = Seq(
      s"$prefix-Type: ${key.typ}",
      s"$prefix-Length: ${key.length}"
    )
    val usage = if (key.usage.nonEmpty) {
      Some(s"$prefix-Usage: ${key.usage.mkString(",")}")
    } else {
      None
    }
    lines ++ usage
  }

  private def nameLines(name: GpgName): Seq[String] = {
    Seq(
      s"Name-Real: ${name.real}",
      s"Name-Email: ${name.email}"
    )
  }
}

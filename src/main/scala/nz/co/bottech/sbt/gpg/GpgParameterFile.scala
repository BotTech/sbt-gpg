package nz.co.bottech.sbt.gpg

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.attribute.{PosixFilePermission, PosixFilePermissions}

import sbt.util.Logger

final case class GpgKeyParameters(length: Int, typ: String, usage: Set[GpgKeyUsage])

final case class GpgName(real: String, email: String)

final case class GpgParameters(key: GpgKeyParameters,
                               subkey: GpgKeyParameters,
                               expire: String,
                               name: GpgName,
                               passphrase: Option[String])

object GpgParameterFile {

  private final val Permissions = {
    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
  }

  def create(parameters: GpgParameters, file: File, log: Logger): File = {
    import parameters.{expire, key, name, passphrase, subkey}
    val lines = {
      keyLines(key, "Key") ++:
        keyLines(subkey, "Subkey") ++:
        s"Expire-Date: $expire" +:
        nameLines(name) ++:
        passphrase.map(p => s"Passphrase: $p").toSeq
    }
    Files.deleteIfExists(file.toPath)
    Option(file.getParentFile).foreach(_.mkdirs())
    Files.createFile(file.toPath, Permissions)
    file.deleteOnExit()
    sbt.IO.writeLines(file, lines, StandardCharsets.UTF_8, append = false)
    log.debug(s"Parameters file written to $file.")
    file
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

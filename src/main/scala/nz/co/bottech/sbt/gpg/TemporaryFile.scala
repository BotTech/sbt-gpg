package nz.co.bottech.sbt.gpg

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

object TemporaryFile {

  private final val Permissions = {
    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
  }

  def create(file: File, lines: Seq[String]): File = {
    Files.deleteIfExists(file.toPath)
    Option(file.getParentFile).foreach(_.mkdirs())
    Files.createFile(file.toPath, Permissions)
    file.deleteOnExit()
    sbt.IO.writeLines(file, lines, StandardCharsets.UTF_8, append = false)
    file
  }
}

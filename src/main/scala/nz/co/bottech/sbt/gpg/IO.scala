package nz.co.bottech.sbt.gpg

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.attribute.{PosixFilePermission, PosixFilePermissions}

import sbt._

object IO {

  private final val Permissions = {
    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
  }

  def createTempFile(file: File, lines: Seq[String]): File = {
    Files.deleteIfExists(file.toPath)
    Option(file.getParentFile).foreach(_.mkdirs())
    Files.createFile(file.toPath, Permissions)
    file.deleteOnExit()
    sbt.IO.writeLines(file, lines, StandardCharsets.UTF_8, append = false)
    file
  }

  def createTempDir(baseDirectory: File, permissions: Set[PosixFilePermission]): File = {
    val dir = sbt.IO.createUniqueDirectory(baseDirectory)
    dir.deleteOnExit()
    dir.setPermissions(permissions)
    dir
  }

  def prepareOutputFile(file: File): File = {
    val parentDir = Option(file.getParentFile)
    parentDir.foreach(_.mkdirs())
    file.delete()
    file
  }
}

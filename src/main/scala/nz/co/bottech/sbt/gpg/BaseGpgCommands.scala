package nz.co.bottech.sbt.gpg

import java.io.File

import sbt.util.Logger

import scala.sys.process._
import scala.util.Try

trait BaseGpgCommands {

  def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)]

  protected def executeVersionCommand(gpg: String,
                                      command: String,
                                      regex: String,
                                      version: GpgVersion,
                                      log: Logger): Either[Throwable, (String, GpgVersion)] = {
    val parts = gpg.split(' ')
    val exe = parts.headOption.getOrElse(gpg)
    val options = parts.tail
    val tryExecute = Try(execute(exe, options, command, log))
    tryExecute.toEither.flatMap { lines =>
      val maybeVersion = lines.find(_.matches(regex))
        .map(_ => version)
      maybeVersion.toRight(GpgUnknownVersionException(s"Expected to find version line: $regex"))
        .map(gpg -> _)
    }
  }

  def commonArguments(homeDirectory: File, statusFileDescriptor: Int): Seq[GpgArgument]

  def generateKey(gpg: String, options: Seq[String], log: Logger): Unit

  def execute(gpg: String, options: Seq[String], command: String, log: Logger): Seq[String] = {
    val processCommand = gpg +: options :+ command
    val processLogger = ProcessLogger(log.error(_))
    val lines = processCommand.lineStream(processLogger)
    lines.map { line =>
      log.info(line)
      line
    }.toList
  }
}

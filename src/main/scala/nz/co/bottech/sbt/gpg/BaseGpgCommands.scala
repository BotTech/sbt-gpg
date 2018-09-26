package nz.co.bottech.sbt.gpg

import java.io.File

import nz.co.bottech.sbt.gpg.BaseGpgCommands._
import nz.co.bottech.sbt.gpg.GpgErrors.{GpgCannotParseOutput, GpgUnknownVersionException}
import sbt.util.Logger

import scala.sys.process._
import scala.util.Try

trait BaseGpgCommands {

  protected val GpgCommandProperty: String
  protected val GpgCommand: String
  protected val GpgVersionRegex: String
  protected val VersionCommand: String
  protected val GenerateKeyCommand: String

  def commandAndVersion(log: Logger): Either[Throwable, (String, GpgVersion)]

  private def splitCommand(gpg: String) = {
    val parts = gpg.split(' ')
    val exe = parts.headOption.getOrElse(gpg)
    val options = parts.tail
    exe -> options
  }

  private def gpgCommand(property: String, default: String): String = {
    val gpg = System.getProperty(property, default)
    val cd = System.getProperty(CurrentDirectory)
    gpg.replaceAllLiterally("$(pwd)", cd)
  }

  protected def executeVersionCommand(version: GpgVersion, log: Logger): Either[Throwable, (String, GpgVersion)] = {
    val gpg = gpgCommand(GpgCommandProperty, GpgCommand)
    val (exe, options) = splitCommand(gpg)
    log.debug(s"Checking version: $exe ${options.mkString(" ")} $VersionCommand")
    val tryExecute = Try(execute(exe, options, VersionCommand, Seq.empty, log))
    tryExecute.toEither.flatMap { lines =>
      val maybeVersion = lines.find(_.matches(GpgVersionRegex))
        .map(_ => version)
      maybeVersion.toRight(GpgUnknownVersionException(s"Expected to find version line: $GpgVersionRegex"))
        .map(gpg -> _)
    }
  }

  def commonArguments(homeDirectory: Option[File], statusFileDescriptor: Int, debug: Boolean): Seq[GpgArgument] = {
    Seq(GpgFlag.verbose).filter(_ => debug) ++
      homeDirectory.map(GpgOption.homeDir) ++
      Seq(
        GpgFlag.batch,
        GpgFlag.withColon,
        GpgOption.statusFD(statusFileDescriptor),
        GpgFlag.noPermissionWarning
      )
  }

  def generateKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): String = {
    log.info(s"Generating key: $gpg ${options.mkString(" ")} $GenerateKeyCommand ${parameters.mkString(" ")}")
    val lines = execute(gpg, options, GenerateKeyCommand, parameters, log)
    val keyID = lines.collectFirst {
      case KeyCreatedPattern(id) => id
    }
    log.warn("You should keep your private master key very, very safe.")
    log.warn("First copy the master key pair to an encrypted external storage device using gpgCopyKey.")
    log.warn("Then delete the private master key from this device using gpgDeletePrivateKey.")
    log.warn(
      "Alternatively you can set gpgHomeDir to the location on the external storage device and then copy the subkey to this device."
    )
    keyID.getOrElse {
      throw GpgCannotParseOutput("Unable to find key Id in the output.")
    }
  }

  def execute(gpg: String, options: Seq[String], command: String, parameters: Seq[String], log: Logger): Seq[String] = {
    val (exe, exeOptions) = splitCommand(gpg)
    val processCommand = exe +: exeOptions ++: options ++: command +: parameters
    val processLogger = ProcessLogger(log.error(_))
    val lines = processCommand.lineStream(processLogger)
    lines.map { line =>
      log.info(line)
      line
    }.toList
  }
}

object BaseGpgCommands {

  final val CurrentDirectory = "user.dir"
  final val KeyCreatedPattern = """\[GNUPG:] KEY_CREATED B (.+)""".r
}

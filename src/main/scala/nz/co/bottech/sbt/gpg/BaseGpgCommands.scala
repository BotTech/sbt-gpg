package nz.co.bottech.sbt.gpg

import java.io.File

import nz.co.bottech.sbt.gpg.BaseGpgCommands._
import nz.co.bottech.sbt.gpg.GpgErrors.{GpgCannotParseOutput, GpgFailedToExecute, GpgUnknownVersionException}
import nz.co.bottech.sbt.gpg.GpgListingParser.GpgListingParseException
import sbt.util.Logger

import scala.sys.process._
import scala.util.{Failure, Success, Try}

trait BaseGpgCommands {

  protected val GpgCommandProperty: String
  protected val GpgCommand: String
  protected val GpgVersionRegex: String

  protected val AddKeyCommand = "--quick-add-key"
  protected val EditKeyCommand = "--edit-key"
  protected val ExportKeyCommand = "--export-secret-keys"
  protected val ExportSubkeyCommand = "--export-secret-subkeys"
  protected val GenerateKeyCommand = "--full-generate-key"
  protected val ImportKeyCommand = "--import"
  protected val ListKeysCommand = "--list-keys"
  protected val SignCommand = "--detach-sig"
  protected val VersionCommand = "--version"

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
    tryExecute.toEither.flatMap { output =>
      val lines = output.std
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
        GpgOption.statusFD(statusFileDescriptor)
      )
  }

  def passphraseArguments(file: File): Seq[GpgArgument] = {
    Seq(
      GpgOption.pinentryMode("loopback"),
      GpgOption.passphraseFile(file)
    )
  }

  def generateKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): String = {
    val output = logAndExecute("Generating key", gpg, options, GenerateKeyCommand, parameters, log)
    val keyFingerprint = parseKeyCreatedFingerprint(output.std)
    log.info(s"Generated your new primary key: $keyFingerprint")
    log.warn("You should keep your private primary key very, very safe.")
    log.warn("First copy the primary key to an encrypted external storage device.")
    log.warn("Then delete the private primary key from this device.")
    keyFingerprint
  }

  def listKeys(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): Seq[GpgKeyInfo] = {
    val output = logAndExecute("Listing keys", gpg, options, ListKeysCommand, parameters, log)
    val listings = GpgListingParser.parseAll(output.std).flatMap {
      case Success(listing) => Some(listing)
      case Failure(GpgListingParseException(message, line)) =>
        log.error(message)
        log.error(s"in: $line")
        log.warn("This listing will be ignored.")
        None
      case Failure(ex) =>
        log.error(ex.getMessage)
        log.warn("This listing will be ignored.")
        None
    }
    GpgKeyInfo.group(listings, log).flatMap {
      case Success(keyInfo) => Some(keyInfo)
      case Failure(ex) =>
        log.error(ex.getMessage)
        log.warn("This key will be ignored.")
        None
    }
  }

  def addKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): String = {
    val output = logAndExecute("Adding subkey", gpg, options, AddKeyCommand, parameters, log)
    val keyFingerprint = parseKeyCreatedFingerprint(output.std)
    log.info(s"Added a new subkey: $keyFingerprint")
    keyFingerprint
  }

  def exportKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): CommandOutput = {
    logAndExecute("Exporting key", gpg, options, ExportKeyCommand, parameters, log)
  }

  def exportSubkey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): CommandOutput = {
    logAndExecute("Exporting subkey", gpg, options, ExportSubkeyCommand, parameters, log)
  }

  def importKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): CommandOutput = {
    logAndExecute("Importing key", gpg, options, ImportKeyCommand, parameters, log)
  }

  def sign(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): CommandOutput = {
    logAndExecute("Signing message", gpg, options, SignCommand, parameters, log)
  }

  def editKey(gpg: String, options: Seq[String], parameters: Seq[String], log: Logger): CommandOutput = {
    logAndExecute("Editing key", gpg, options, EditKeyCommand, parameters, log)
  }

  private def logAndExecute(message: String,
                            gpg: String,
                            options: Seq[String],
                            command: String,
                            parameters: Seq[String],
                            log: Logger): CommandOutput = {
    log.info(s"$message: $gpg ${options.mkString(" ")} $command ${parameters.mkString(" ")}")
    execute(gpg, options, command, parameters, log)
  }

  def execute(gpg: String, options: Seq[String], command: String, parameters: Seq[String], log: Logger): CommandOutput = {
    val (exe, exeOptions) = splitCommand(gpg)
    val processCommand = exe +: exeOptions ++: options ++: command +: parameters
    val processLogger = new SimpleProcessLogger(log)
    val exitCode = processCommand ! processLogger
    if (exitCode != 0) {
      throw GpgFailedToExecute("GnuPG returned a nonzero exit code: " + exitCode)
    }
    CommandOutput(processLogger.output, processLogger.error)
  }
}

object BaseGpgCommands {

  final case class CommandOutput(std: Seq[String], err: Seq[String])

  final val CurrentDirectory = "user.dir"
  // Output format is listed in https://git.gnupg.org/cgi-bin/gitweb.cgi?p=gnupg.git;a=blob_plain;f=doc/DETAILS
  final val KeyCreatedPattern = """\[GNUPG:] KEY_CREATED (B|P|S) ([0-9A-F]{40})(?: (.*))?""".r

  private def parseKeyCreatedFingerprint(lines: Seq[String]): String = {
    val maybeKeyFingerprint = lines.collectFirst {
      case KeyCreatedPattern(_, id, _) => id
    }
    maybeKeyFingerprint.getOrElse {
      throw GpgCannotParseOutput("Unable to find key fingerprint in the output.")
    }
  }
}

package nz.co.bottech.sbt.gpg

import sbt.util.Logger

import scala.collection.mutable.ListBuffer
import scala.sys.process.ProcessLogger

class SimpleProcessLogger(log: Logger) extends ProcessLogger {

  private[this] val outBuffer = ListBuffer.empty[String]
  private[this] val errBuffer = ListBuffer.empty[String]

  override def out(s: => String): Unit = synchronized {
    val message = s
    outBuffer += message
    log.info(message)
  }

  override def err(s: => String): Unit = synchronized {
    val message = s
    errBuffer += message
    log.error(message)
  }

  override def buffer[T](f: => T): T = f

  def output: List[String] = synchronized {
    outBuffer.toList
  }

  def error: List[String] = synchronized {
    errBuffer.toList
  }
}

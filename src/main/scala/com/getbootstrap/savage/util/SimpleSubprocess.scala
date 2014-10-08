package com.getbootstrap.savage.util

import java.io.File

object SimpleSubprocess {
  private val devNull = new File("/dev/null")
  private val rootDir = new File("/")
}
case class SimpleSubprocess(args: Seq[String]) {
  val processBuilder = (
    new ProcessBuilder(args : _*)
    .redirectErrorStream(true)
    .redirectInput(ProcessBuilder.Redirect.from(SimpleSubprocess.devNull))
  )
  def run(): SubprocessResult = {
    val process = processBuilder.start()
    val output = process.getInputStream.readUntilEofAsSingleUtf8String
    val exitValue = process.waitFor()
    SubprocessResult(exitValue, output)
  }
}

object SubprocessResult {
  def apply(exitValue: Int, output: String): SubprocessResult = {
    exitValue match {
      case 0 => SuccessfulExit(output)
      case _ => ErrorExit(exitValue, output)
    }
  }
}
sealed trait SubprocessResult {
  def exitValue: Int
}
case class SuccessfulExit(output: String) extends SubprocessResult {
  override def exitValue = 0
}
case class ErrorExit(exitValue: Int, output: String) extends SubprocessResult

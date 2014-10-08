package com.getbootstrap.savage.util

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path, SimpleFileVisitor, FileVisitResult}
import akka.event.LoggingAdapter

class DeleterFileVisitor(private val log: LoggingAdapter) extends SimpleFileVisitor[Path] {
  @throws[SecurityException]
  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
    tryToDelete(file)
    FileVisitResult.CONTINUE
  }

  @throws[IOException]
  @throws[SecurityException]
  override def postVisitDirectory(dir: Path, maybeExc: IOException): FileVisitResult = {
    maybeExc match {
      case null => {
        tryToDelete(dir)
        FileVisitResult.CONTINUE
      }
      case exc => throw exc
    }
  }

  @throws[SecurityException]
  private def tryToDelete(path: Path): Boolean = {
    try {
      Files.deleteIfExists(path)
    }
    catch {
      case exc:IOException => {
        log.error(exc, s"Problem deleting ${path}")
        false
      }
    }
  }
}

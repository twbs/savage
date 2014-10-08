package com.getbootstrap.savage.util

import java.nio.file.Path
import akka.event.LoggingAdapter

class FilePathWatchlist(globs: Iterable[String]) {
  private val matchers = globs.toIterator.map{ _.asUnixGlob }.toVector
  def isInteresting(path: Path)(implicit log: LoggingAdapter): Boolean = {
    val interesting = matchers.exists{ _.matches(path) }
    if (interesting) {
      log.info(s"Interesting path: ${path}")
    }
    interesting
  }
  def anyInterestingIn(paths: Iterable[Path])(implicit log: LoggingAdapter) = {
    paths.find{ path => isInteresting(path) } match {
      case Some(path) => true
      case None => {
        log.info("No interesting paths found.")
        false
      }
    }
  }
}

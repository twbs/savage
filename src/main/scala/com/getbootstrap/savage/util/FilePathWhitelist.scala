package com.getbootstrap.savage.util

import java.nio.file.Path
import akka.event.LoggingAdapter

class FilePathWhitelist(globs: Iterable[String]) {
  private val matchers = globs.toIterator.map{ _.asUnixGlob }.toVector
  def isAllowed(path: Path)(implicit log: LoggingAdapter): Boolean = {
    var allowed = matchers.exists{ _.matches(path) }
    if (!allowed) {
      log.info(s"Path disallowed by whitelist: ${path}")
    }
    allowed
  }
}

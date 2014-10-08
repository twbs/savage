package com.getbootstrap.savage.github

object CommitSha {
  private val ShaRegex = "^[0-9a-f]{40}$".r
  def apply(sha: String): Option[CommitSha] = {
    sha match {
      case ShaRegex(_*) => Some(new CommitSha(sha))
      case _ => None
    }
  }
}

class CommitSha private(val sha: String) extends AnyVal {
  override def toString = s"CommitSha(${sha})"
}

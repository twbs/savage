package com.getbootstrap.savage.travis

import scala.util.{Try,Success,Failure}
import spray.http.Uri
import com.getbootstrap.savage.github.{Branch, CommitSha}
import com.getbootstrap.savage.travis.build_status.BuildStatus

case class TravisPayload(
  status_message: String,
  build_url: String,
  branch: String,
  commit: String
) {
  def status: BuildStatus = BuildStatus(status_message).getOrElse{ throw new IllegalStateException(s"Invalid Travis build status message: ${status_message}") }
  def commitSha: CommitSha = CommitSha(commit).getOrElse{ throw new IllegalStateException(s"Invalid commit SHA: ${commit}") }
  def branchName: Branch = Branch(branch).getOrElse{ throw new IllegalStateException(s"Unsafe branch name: ${branch}") }
  def buildUrl: String = Try{ Uri(build_url) }.flatMap{ FairlySafeUrl(_) }.get
}

object FairlySafeUrl {
  private val SafeishUrlRegex = "^[a-zA-Z0-9/:_-]+$".r
  def apply(url: Uri): Try[String] = {
    val cleanUrl = url.withScheme("https").withQuery(Uri.Query.Empty).withoutFragment.toString
    cleanUrl match {
      case SafeishUrlRegex(_*) => Success(cleanUrl)
      case _ => Failure(new IllegalStateException(s"Travis URL failed safety check; URL: ${cleanUrl}"))
    }

  }
}

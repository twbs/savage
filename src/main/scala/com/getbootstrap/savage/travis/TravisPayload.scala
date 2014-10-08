package com.getbootstrap.savage.travis

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
}

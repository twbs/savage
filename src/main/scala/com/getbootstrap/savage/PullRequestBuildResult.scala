package com.getbootstrap.savage

import com.getbootstrap.savage.github._

case class PullRequestBuildResult(
  prNum: PullRequestNumber,
  commitSha: CommitSha,
  buildUrl: String,
  succeeded: Boolean
)

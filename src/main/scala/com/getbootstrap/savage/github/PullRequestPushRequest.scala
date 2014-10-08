package com.getbootstrap.savage.github

import org.eclipse.egit.github.core.RepositoryId

case class PullRequestPushRequest(
  origin: RepositoryId,
  number: PullRequestNumber,
  commitSha: CommitSha
)

package com.getbootstrap.savage.github

import org.eclipse.egit.github.core._

package object util {
  private val SafeRepoRegex = "^[0-9a-zA-Z_-]+/[0-9a-zA-Z_-]+$".r

  implicit class RichRepository(repo: Repository) {
    def repositoryId: Option[RepositoryId] = {
      val repoId = new RepositoryId(repo.getOwner.getLogin, repo.getName)
      repo.generateId match {
        case SafeRepoRegex(_*) => Some(repoId)
        case _ => None
      }
    }
  }
  implicit class RichPullRequestMarker(marker: PullRequestMarker) {
    def commitSha: CommitSha = CommitSha(marker.getSha).getOrElse{ throw new IllegalStateException(s"Invalid commit SHA: ${marker.getSha}") }
  }
  implicit class RichCommitFile(file: CommitFile) {

  }
  implicit class RichPullRequest(pr: PullRequest) {
    def number: PullRequestNumber = PullRequestNumber(pr.getNumber).get
  }
  implicit class RichRepositoryId(repoId: RepositoryId) {
    def asPushRemote: String = s"git@github.com:${repoId.generateId}.git"
    def asPullRemote: String = s"https://github.com/${repoId.generateId}.git"
  }
}

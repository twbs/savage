package com.getbootstrap.savage.github

import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.event.PullRequestPayload
import com.getbootstrap.savage.github.pr_action.PullRequestAction

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
    import org.eclipse.egit.github.core.service.IssueService
    def number: PullRequestNumber = PullRequestNumber(pr.getNumber).get
    def isOpen: Boolean = (pr.getState == IssueService.STATE_OPEN)
  }
  implicit class RichRepositoryId(repoId: RepositoryId) {
    def asPushRemote: String = s"git@github.com:${repoId.generateId}.git"
    def asPullRemote: String = s"https://github.com/${repoId.generateId}.git"
  }
  implicit class RichPullRequestPayload(payload: PullRequestPayload) {
    def action: PullRequestAction = PullRequestAction(payload.getAction).get
  }
}

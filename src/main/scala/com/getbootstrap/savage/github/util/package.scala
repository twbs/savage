package com.getbootstrap.savage.github

import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.event.PullRequestPayload
import com.getbootstrap.savage.github.pr_action.PullRequestAction

package object util {
  private val SafeRepoRegex = "^[0-9a-zA-Z_.-]+/[0-9a-zA-Z_.-]+$".r

  implicit class RichRepository(val repo: Repository) extends AnyVal {
    def repositoryId: Option[RepositoryId] = {
      val repoId = new RepositoryId(repo.getOwner.getLogin, repo.getName)
      repo.generateId match {
        case SafeRepoRegex(_*) => Some(repoId)
        case _ => None
      }
    }
  }
  implicit class RichPullRequestMarker(val marker: PullRequestMarker) extends AnyVal {
    def commitSha: CommitSha = CommitSha(marker.getSha).getOrElse{ throw new IllegalStateException(s"Invalid commit SHA: ${marker.getSha}") }
    def branch: Option[Branch] = Branch(marker.getRef)
  }
  implicit class RichCommitFile(val file: CommitFile) extends AnyVal {

  }
  implicit class RichPullRequest(val pr: PullRequest) extends AnyVal {
    import org.eclipse.egit.github.core.service.IssueService
    def number: PullRequestNumber = PullRequestNumber(pr.getNumber).get
    def isOpen: Boolean = (pr.getState == IssueService.STATE_OPEN)
  }
  implicit class RichRepositoryId(val repoId: RepositoryId) extends AnyVal {
    def asPushRemote: String = s"git@github.com:${repoId.generateId}.git"
    def asPullRemote: String = s"https://github.com/${repoId.generateId}.git"
  }
  implicit class RichPullRequestPayload(val payload: PullRequestPayload) extends AnyVal {
    def action: PullRequestAction = PullRequestAction(payload.getAction).get
  }
  implicit class RichUser(val user: User) extends AnyVal {
    def username: GitHubUser = GitHubUser(user.getLogin)
  }
}

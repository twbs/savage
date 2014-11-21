package com.getbootstrap.savage.server

import scala.util.{Failure, Success, Try}
import org.eclipse.egit.github.core.service.IssueService
import com.getbootstrap.savage.github.{GitHubActorWithLogging, PullRequestNumber}
import com.getbootstrap.savage.PullRequestBuildResult

class PullRequestCommenter extends GitHubActorWithLogging {
  private def tryToCommentOn(prNum: PullRequestNumber, commentMarkdown: String) = {
    val issueService = new IssueService(gitHubClient)
    Try { issueService.createComment(settings.MainRepoId, prNum.number, commentMarkdown) }
  }

  override def receive = {
    case PullRequestBuildResult(prNum, commitSha, buildUrl, succeeded) => {
      val mythicalStatus = if (succeeded) { "**CONFIRMED**" } else { "**BUSTED**" }
      val plainStatus = if (succeeded) { "**Tests passed.**" } else { "**Tests failed.**" }

      val commentMarkdown = s"""
        |${plainStatus} Automated cross-browser testing via Sauce Labs and Travis CI shows that the JavaScript changes in this pull request are: ${mythicalStatus}
        |
        |Commit: ${commitSha.sha}
        |Build details: ${buildUrl}
        |
        |(*Please note that this is a [fully automated](https://github.com/twbs/savage) comment.*)
      """.stripMargin

      tryToCommentOn(prNum, commentMarkdown) match {
        case Success(comment) => log.info(s"Successfully posted comment ${comment.getUrl} for ${prNum}")
        case Failure(exc) => log.error(exc, s"Error posting comment for ${prNum}")
      }
    }
  }
}

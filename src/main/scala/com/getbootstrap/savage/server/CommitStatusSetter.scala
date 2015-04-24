package com.getbootstrap.savage.server

import scala.util.{Try,Success,Failure}
import org.eclipse.egit.github.core.{CommitStatus=>RawCommitStatus}
import org.eclipse.egit.github.core.service.CommitService
import com.getbootstrap.savage.github.commit_status.StatusForCommit
import com.getbootstrap.savage.github.GitHubActorWithLogging

class CommitStatusSetter extends GitHubActorWithLogging {
  def tryToSetCommitStatus(commitStatus: StatusForCommit): Try[RawCommitStatus] = {
    val commitService = new CommitService(gitHubClient)
    val commitSha = commitStatus.commit.sha
    val status = commitStatus.status.asRawStatus
    Try { commitService.createStatus(settings.MainRepoId, commitSha, status) }
  }

  override def receive = {
    case commitStatus@StatusForCommit(commit, status) => {
      if (settings.SetCommitStatus) {
        tryToSetCommitStatus(commitStatus) match {
          case Success(createdCommitStatus) => {
            log.info(s"Successfully created commit status with state ${status.name} for ${commit}")
          }
          case Failure(exc) => log.error(exc, s"Error setting ${commitStatus}")
        }
      }
    }
  }
}

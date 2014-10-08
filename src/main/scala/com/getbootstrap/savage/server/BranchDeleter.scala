package com.getbootstrap.savage.server

import scala.collection.JavaConverters._
import org.eclipse.egit.github.core.service.RepositoryService
import com.getbootstrap.savage.github.{GitHubActorWithLogging, Branch, BranchDeletionRequest}
import com.getbootstrap.savage.github.util.RichRepositoryId
import com.getbootstrap.savage.util.{SuccessfulExit, ErrorExit, SimpleSubprocess}

class BranchDeleter extends GitHubActorWithLogging {
  override def receive = {
    case BranchDeletionRequest(branch, commitSha) => {
      if (isSavageBranch(branch)) {
        val repoService = new RepositoryService(gitHubClient)
        val maybeRepoBranch = repoService.getBranches(settings.TestRepoId).asScala.find{ _.getName == branch.name }
        maybeRepoBranch match {
          case None => log.info(s"Nothing to delete; ${branch} does not exist in ${settings.TestRepoId}")
          case Some(repoBranch) => {
            val repoSha = repoBranch.getCommit.getSha
            if (repoSha == commitSha.sha) {
              val remote = settings.TestRepoId.asPushRemote
              val process = SimpleSubprocess(Seq("git", "push", remote, ":" + branch.name))
              log.info(s"Deleting branch ${branch} from remote ${remote}")
              process.run() match {
                case SuccessfulExit(_) => log.info(s"Successfully deleted ${branch} in ${remote}")
                case ErrorExit(exitValue, output) => log.error(s"Error deleting ${branch} in ${remote} :\nExit code: ${exitValue}\n${output}")
              }
            }
            else {
              log.info(s"Not deleting ${branch} from ${settings.TestRepoId} because commits differ; request was for ${commitSha} but current is ${repoSha}")
            }
          }
        }
      }
      else {
        log.error(s"Not deleting non-Savage branch : ${branch}")
      }
    }
  }

  private def isSavageBranch(branch: Branch): Boolean = branch.name startsWith settings.BranchPrefix
}

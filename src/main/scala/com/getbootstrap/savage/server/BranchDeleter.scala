package com.getbootstrap.savage.server

import scala.collection.JavaConverters._
import com.getbootstrap.savage.github.{SavageBranch, GitHubActorWithLogging}
import com.getbootstrap.savage.github.util.RichRepositoryId
import com.getbootstrap.savage.util.{SuccessfulExit, ErrorExit, SimpleSubprocess}

class BranchDeleter extends GitHubActorWithLogging {
  override def receive = {
    case branch:SavageBranch => {
      val repoService = new RepositoryService()
      gitHubClient.repos.get(settings.TestRepoId).br
      val maybeRepoBranch = repoService.getBranches(settings.TestRepoId).asScala.find{ _.getName == branch.branch.name }
      maybeRepoBranch match {
        case None => log.info(s"Nothing to delete; ${branch} does not exist in ${settings.TestRepoId}")
        case Some(repoBranch) => {
          val remote = settings.TestRepoId.asPushRemote
          val process = SimpleSubprocess(Seq("git", "push", remote, ":" + branch.branch.name))
          log.info(s"Deleting ${branch} from remote ${remote}")
          process.run() match {
            case SuccessfulExit(_) => log.info(s"Successfully deleted ${branch} in ${remote}")
            case ErrorExit(exitValue, output) => log.error(s"Error deleting ${branch} in ${remote} :\nExit code: ${exitValue}\n${output}")
          }
        }
      }
    }
  }
}

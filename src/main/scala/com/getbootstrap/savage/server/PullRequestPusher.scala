package com.getbootstrap.savage.server

import org.eclipse.egit.github.core.RepositoryId
import com.getbootstrap.savage.github._
import com.getbootstrap.savage.github.util.RichRepositoryId
import com.getbootstrap.savage.util.{ErrorExit, SuccessfulExit, SimpleSubprocess}
import com.getbootstrap.savage.util.{RichPath,UnixFileSystemString}

class PullRequestPusher extends GitHubActorWithLogging {
  private val gitRemoteRefsDirectory = ".git/refs/remotes".asUnixPath

  override def receive = {
    case PullRequestPushRequest(originRepo, prNum, commitSha) => {
      if (pull(originRepo)) {
        push(originRepo = originRepo, prNum = prNum, commitSha = commitSha)
      }
    }
  }

  def pull(originRepo: RepositoryId): Boolean = {
    // clobberingly fetch all branch heads into a dummy remote
    SimpleSubprocess(Seq("git", "fetch", "--no-tags", "--recurse-submodules=no", originRepo.asPullRemote, "+refs/heads/*:refs/remotes/scratch/*")).run() match {
      case SuccessfulExit(_) => {
        log.info(s"Successfully fetched from ${originRepo}")
        true
      }
      case ErrorExit(exitValue, output) => {
        log.error(s"Error fetching from ${originRepo}:\nExit code: ${exitValue}\n${output}")
        false
      }
    }
  }

  def push(originRepo: RepositoryId, prNum: PullRequestNumber, commitSha: CommitSha): Boolean = {
    val newBranch = {
      val branchName = settings.BranchPrefix + prNum.number
      Branch(branchName).getOrElse {
        throw new SecurityException("Generated insecure branch name: ${}")
      }
    }
    val branchSpec = s"${commitSha.sha}:${newBranch.asRef}"
    val destRemote = settings.TestRepoId.asPushRemote
    val success = SimpleSubprocess(Seq("git", "push", destRemote, branchSpec)).run() match {
      case SuccessfulExit(_) => {
        log.info(s"Successfully pushed ${commitSha} from ${originRepo} to ${destRemote} as ${newBranch}")
        true
      }
      case ErrorExit(exitValue, output) => {
        log.error(s"Error pushing ${commitSha} from ${originRepo} to ${destRemote} as ${newBranch}:\nExit code: ${exitValue}\n${output}")
        false
      }
    }
    // delete all remote refs
    implicit val logger = log
    gitRemoteRefsDirectory.deleteRecursively()
    success
  }
}

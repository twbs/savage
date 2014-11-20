package com.getbootstrap.savage.server

import java.nio.file.Path
import scala.collection.JavaConverters._
import scala.util.{Try,Success,Failure}
import akka.actor.ActorRef
import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.service.CommitService
import com.getbootstrap.savage.github._
import com.getbootstrap.savage.github.util._
import com.getbootstrap.savage.util.UnixFileSystemString

class PullRequestEventHandler(protected val pusher: ActorRef) extends GitHubActorWithLogging {

  private def affectedFilesFor(repoId: RepositoryId, base: CommitSha, head: CommitSha): Try[Set[Path]] = {
    val commitService = new CommitService(gitHubClient)
    Try { commitService.compare(repoId, base.sha, head.sha) }.map { comparison =>
      val affectedFiles = comparison.getFiles.asScala.map{ "/" + _.getFilename }.toSet[String].map{ _.asUnixPath }
      affectedFiles
    }
  }

  private val NormalPathRegex = "^[a-zA-Z0-9_./-]+$".r
  private def isNormal(path: Path): Boolean = {
    path.toString match {
      case NormalPathRegex(_*) => true
      case _ => {
        log.info(s"Abnormal path: ${path}")
        false
      }
    }
  }
  private def areSafe(paths: Set[Path]): Boolean = {
    implicit val logger = log
    paths.forall{ path => isNormal(path) && settings.Whitelist.isAllowed(path) }
  }
  private def areInteresting(paths: Set[Path]): Boolean = {
    implicit val logger = log
    settings.Watchlist.anyInterestingIn(paths)
  }

  private def logPrInfo(msg: String)(implicit prNum: PullRequestNumber) {
    log.info(s"PR #${prNum.number} : ${msg}")
  }

  override def receive = {
    case pr: PullRequest => {
      implicit val prNum = pr.number
      val bsBase = pr.getBase
      val prHead = pr.getHead
      val destinationRepo = bsBase.getRepo.repositoryId
      destinationRepo match {
        case None => log.error(s"Received event from GitHub about irrelevant repository with unsafe name")
        case Some(settings.MainRepoId) => {
          val destBranch = bsBase.getRef
          destBranch match {
            case "master" => {
              prHead.getRepo.repositoryId match {
                case None => log.error(s"Received event from GitHub about repository with unsafe name")
                case Some(foreignRepo) => {
                  val baseSha = bsBase.commitSha
                  val headSha = prHead.commitSha

                  affectedFilesFor(foreignRepo, baseSha, headSha) match {
                    case Failure(exc) => {
                      log.error(exc, s"Could not get affected files for commits ${baseSha}...${headSha} for ${foreignRepo}")
                    }
                    case Success(affectedFiles) => {
                      log.debug("Files affected by {}: {}", prNum, affectedFiles)
                      if (areSafe(affectedFiles)) {
                        if (areInteresting(affectedFiles)) {
                          logPrInfo(s"Requesting build for safe & interesting PR")
                          pusher ! PullRequestPushRequest(
                            origin = foreignRepo,
                            number = pr.number,
                            commitSha = headSha
                          )
                        }
                        else {
                          logPrInfo(s"Ignoring PR with no interesting file changes")
                        }
                      }
                      else {
                        logPrInfo(s"Ignoring PR with unsafe file changes")
                      }
                    }
                  }
                }
              }
            }
            case _ => logPrInfo(s"Ignoring since PR targets the ${destBranch} branch")
          }
        }
        case Some(otherRepo) => log.error(s"Received event from GitHub about irrelevant repository: ${otherRepo}")
      }
    }
  }
}

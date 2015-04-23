package com.getbootstrap.savage.server

import scala.util.{Try,Success,Failure}
import akka.actor.ActorRef
import spray.routing._
import spray.http._
import com.getbootstrap.savage.PullRequestBuildResult
import com.getbootstrap.savage.github.{BranchDeletionRequest, PullRequestNumber, commit_status, pr_action, event=>events}
import com.getbootstrap.savage.github.util._

class SavageWebService(
  protected val pullRequestEventHandler: ActorRef,
  protected val pullRequestCommenter: ActorRef,
  protected val branchDeleter: ActorRef,
  protected val statusSetter: ActorRef
) extends ActorWithLogging with HttpService {
  import GitHubWebHooksDirectives.{gitHubEvent,authenticatedPullRequestEvent,authenticatedIssueOrCommentEvent}
  import TravisWebHookDirectives.authenticatedTravisEvent

  private val settings = Settings(context.system)
  override def actorRefFactory = context
  override def receive = runRoute(theOnlyRoute)

  val theOnlyRoute =
    pathPrefix("savage") {
      pathEndOrSingleSlash {
        get {
          complete(StatusCodes.OK, "Hi! Savage is online.")
        }
      } ~
      path("github") {
        pathEndOrSingleSlash {
          post {
            gitHubEvent(log) { ghEvent =>
              ghEvent match {
                case events.Ping => {
                  log.info("Successfully received GitHub webhook ping.")
                  complete(StatusCodes.OK)
                }
                case events.IssueComment => {
                  authenticatedIssueOrCommentEvent(settings.GitHubWebHookSecretKey.toArray) { event => {
                    pullRequestEventHandler ! event
                    complete(StatusCodes.OK)
                  }}
                }
                case events.PullRequest => {
                  authenticatedPullRequestEvent(settings.GitHubWebHookSecretKey.toArray) { event =>
                    event.action match {
                      case pr_action.Opened | pr_action.Synchronize => {
                        val pr = event.getPullRequest
                        if (pr.isOpen) {
                          pullRequestEventHandler ! pr
                          complete(StatusCodes.OK)
                        }
                        else {
                          complete(StatusCodes.OK, s"Ignoring event about closed pull request #${pr.getId}")
                        }
                      }
                      case _ => complete(StatusCodes.OK, "Ignoring irrelevant action")
                    }
                  }
                }
                case _ => complete(StatusCodes.BadRequest, "Unexpected event type")
              }
            }
          }
        }
      } ~
      path("travis") {
        pathEndOrSingleSlash {
          post {
            authenticatedTravisEvent(travisToken = settings.TravisToken, repo = settings.TestRepoId, log = log) { event =>
              if (event.branchName.name.startsWith(settings.BranchPrefix)) {
                Try { Integer.parseInt(event.branchName.name.stripPrefix(settings.BranchPrefix)) }.flatMap{ intStr => Try{ PullRequestNumber(intStr).get } } match {
                  case Failure(exc) => log.error(exc, s"Invalid Savage branch name from Travis event: ${event.branchName}")
                  case Success(prNum) => {
                    branchDeleter ! BranchDeletionRequest(event.branchName, event.commitSha)
                    val commitStatus = if (event.status.isSuccessful) {
                      commit_status.Success("CONFIRMED: Savage cross-browser JS testing passed", event.buildUrl)
                    } else {
                      commit_status.Failure("BUSTED: Savage cross-browser JS testing failed", event.buildUrl)
                    }
                    statusSetter ! commitStatus
                    pullRequestCommenter ! PullRequestBuildResult(
                      prNum = prNum,
                      commitSha = event.commitSha,
                      buildUrl = event.buildUrl,
                      succeeded = event.status.isSuccessful
                    )
                  }
                }
              }
              else {
                log.info(s"Ignoring authentic Travis event from irrelevant ${event.branchName}")
              }
              complete(StatusCodes.OK)
            }
          }
        }
      }
    }
}

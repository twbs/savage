package com.getbootstrap.savage.server

import scala.util.{Try,Success,Failure}
import akka.actor.ActorRef
import spray.routing._
import spray.http._
import com.getbootstrap.savage.PullRequestBuildResult
import com.getbootstrap.savage.github.{BranchDeletionRequest, PullRequestNumber}

class SavageWebService(
  protected val pullRequestEventHandler: ActorRef,
  protected val pullRequestCommenter: ActorRef,
  protected val branchDeleter: ActorRef
) extends ActorWithLogging with HttpService {
  import GitHubWebHooksDirectives.{authenticatedPullRequestEvent,authenticatedIssueOrCommentEvent}
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
            headerValueByName("X-Github-Event") { githubEvent =>
              githubEvent match {
                case "ping" => {
                  log.info("Successfully received GitHub webhook ping.")
                  complete(StatusCodes.OK)
                }
                case "issue_comment" => {
                  authenticatedIssueOrCommentEvent(settings.GitHubWebHookSecretKey.toArray) { event => {
                    pullRequestEventHandler ! event
                    complete(StatusCodes.OK)
                  }}
                }
                case "pull_request" => {
                  authenticatedPullRequestEvent(settings.GitHubWebHookSecretKey.toArray) { event =>
                    event.getAction match {
                      case "opened" | "synchronize" => {
                        val pr = event.getPullRequest
                        if (pr.getState == "open") {
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
                    pullRequestCommenter ! PullRequestBuildResult(
                      prNum = prNum,
                      commitSha = event.commitSha,
                      buildUrl = event.buildUrl,
                      succeeded = event.status.isSuccessful
                    )
                  }
                }
              }
              complete(StatusCodes.OK)
            }
          }
        }
      }
    }
}

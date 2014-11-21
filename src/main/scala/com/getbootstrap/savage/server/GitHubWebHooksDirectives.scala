package com.getbootstrap.savage.server

import scala.util.{Success, Failure, Try}
import spray.json._
import spray.routing.{Directive1, ValidationRejection}
import spray.routing.directives.{BasicDirectives, RouteDirectives}
import org.eclipse.egit.github.core.event.PullRequestPayload
import org.eclipse.egit.github.core.client.GsonUtils
import com.getbootstrap.savage.github.{GitHubJsonProtocol,IssueOrCommentEvent}

trait GitHubWebHooksDirectives {
  import RouteDirectives.reject
  import BasicDirectives.provide
  import HubSignatureDirectives.stringEntityMatchingHubSignature
  import GitHubJsonProtocol._

  def authenticatedPullRequestEvent(secretKey: Array[Byte]): Directive1[PullRequestPayload] = stringEntityMatchingHubSignature(secretKey).flatMap{ entityJsonString =>
    Try { GsonUtils.fromJson(entityJsonString, classOf[PullRequestPayload]) } match {
      case Failure(exc) => reject(ValidationRejection("JSON was either malformed or did not match expected schema!"))
      case Success(payload) => provide(payload)
    }
  }

  def authenticatedIssueOrCommentEvent(secretKey: Array[Byte]): Directive1[IssueOrCommentEvent] = stringEntityMatchingHubSignature(secretKey).flatMap{ entityJsonString =>
    Try{ entityJsonString.parseJson.convertTo[IssueOrCommentEvent] } match {
      case Failure(err) => reject(ValidationRejection("JSON either malformed or does not match expected schema!"))
      case Success(event) => provide(event)
    }
  }
}

object GitHubWebHooksDirectives extends GitHubWebHooksDirectives

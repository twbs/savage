package com.getbootstrap.savage.server

import scala.util.{Success, Failure, Try}
import akka.event.LoggingAdapter
import spray.json._
import spray.routing.{Directive1, ValidationRejection}
import spray.routing.directives.{BasicDirectives, RouteDirectives, HeaderDirectives}
import org.eclipse.egit.github.core.event.PullRequestPayload
import org.eclipse.egit.github.core.client.GsonUtils
import com.getbootstrap.savage.github.{GitHubJsonProtocol,IssueOrCommentEvent}
import com.getbootstrap.savage.github.event.Event

trait GitHubWebHooksDirectives {
  import RouteDirectives.reject
  import BasicDirectives.provide
  import HeaderDirectives.headerValueByName
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

  def gitHubEvent(log: LoggingAdapter): Directive1[Event] = headerValueByName("X-Github-Event").flatMap{ eventName => {
    Event(eventName) match {
      case None => {
        log.error(s"Received unknown GitHub event: ${eventName}")
        reject(ValidationRejection(s"Unrecognized GitHub event: ${eventName}"))
      }
      case Some(event) => provide(event)
    }
  }}
}

object GitHubWebHooksDirectives extends GitHubWebHooksDirectives

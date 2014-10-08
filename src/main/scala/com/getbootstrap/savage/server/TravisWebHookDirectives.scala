package com.getbootstrap.savage.server

import scala.util.{Success, Failure, Try}
import akka.event.LoggingAdapter
import spray.routing.{Directive1, ValidationRejection}
import spray.routing.directives.{BasicDirectives, RouteDirectives}
import spray.json._
import org.eclipse.egit.github.core.RepositoryId
import com.getbootstrap.savage.travis.{TravisJsonProtocol, TravisPayload}

trait TravisWebHookDirectives {
  import RouteDirectives.reject
  import BasicDirectives.provide
  import TravisAuthDirectives.stringEntityIfTravisAuthValid
  import TravisJsonProtocol._

  def authenticatedTravisEvent(travisToken: String, repo: RepositoryId, log: LoggingAdapter): Directive1[TravisPayload] = stringEntityIfTravisAuthValid(travisToken, repo).flatMap{ entityJsonString =>
    Try { entityJsonString.parseJson.convertTo[TravisPayload] } match {
      case Failure(exc) => reject(ValidationRejection("JSON was either malformed or did not match expected schema!"))
      case Success(payload) => provide(payload)
    }
  }
}

object TravisWebHookDirectives extends TravisWebHookDirectives

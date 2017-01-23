package com.getbootstrap.savage.server

import scala.util.{Success, Failure, Try}
import akka.event.LoggingAdapter
import spray.routing.{Directive1, ValidationRejection}
import spray.routing.directives.{BasicDirectives, RouteDirectives}
import spray.json._
import org.eclipse.egit.github.core.RepositoryId
import com.getbootstrap.savage.crypto.RsaPublicKey
import com.getbootstrap.savage.travis.{TravisJsonProtocol, TravisPayload}

trait TravisWebHookDirectives {
  import RouteDirectives.reject
  import BasicDirectives.provide
  import TravisSignatureDirectives.stringEntityIfTravisSignatureValid
  import TravisJsonProtocol._

  def authenticatedTravisEvent(travisPublicKey: RsaPublicKey, testRepo: RepositoryId, log: LoggingAdapter): Directive1[TravisPayload] = stringEntityIfTravisSignatureValid(travisPublicKey, log).flatMap{ entityJsonString =>
    Try { entityJsonString.parseJson.convertTo[TravisPayload] } match {
      case Failure(exc) => {
        log.error("Received Travis request with bad JSON!")
        reject(ValidationRejection("JSON was either malformed or did not match expected schema!"))
      }
      case Success(payload) => {
        val TestRepo = testRepo
        payload.repository.id match {
          case TestRepo => provide(payload)
          case otherRepo => {
            reject(ValidationRejection(s"Received Travis request regarding irrelevant repo ${otherRepo}"))
          }
        }
      }
    }
  }
}

object TravisWebHookDirectives extends TravisWebHookDirectives

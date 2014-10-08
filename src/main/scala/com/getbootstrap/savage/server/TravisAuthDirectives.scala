package com.getbootstrap.savage.server

import scala.util.Try
import spray.http.FormData
import spray.routing.{Directive1, MalformedHeaderRejection, MalformedRequestContentRejection, ValidationRejection}
import spray.routing.directives.{BasicDirectives, HeaderDirectives, RouteDirectives, MarshallingDirectives}
import org.eclipse.egit.github.core.RepositoryId
import com.getbootstrap.savage.util.{Sha256,Utf8String}

trait TravisAuthDirectives {
  import BasicDirectives.provide
  import HeaderDirectives.headerValueByName
  import RouteDirectives.reject
  import MarshallingDirectives.{entity, as}

  private val authorization = "Authorization"
  private val authorizationHeaderValue = headerValueByName(authorization)

  val travisAuthorization: Directive1[Array[Byte]] = authorizationHeaderValue.flatMap { hex =>
    Try{ javax.xml.bind.DatatypeConverter.parseHexBinary(hex) }.toOption match {
      case Some(bytesFromHex) => provide(bytesFromHex)
      case None => reject(MalformedHeaderRejection(authorization, "Malformed SHA-256 hex digest"))
    }
  }

  private val formDataEntity = entity(as[FormData])

  def stringEntityIfTravisAuthValid(travisToken: String, repo: RepositoryId): Directive1[String] = travisAuthorization.flatMap { hash =>
    formDataEntity.flatMap { formData =>
      val plainText = repo.generateId + travisToken
      val auth = new Sha256(hash = hash, plainText = plainText.utf8Bytes)
      if (auth.isValid) {
        formData.fields.toMap.get("payload") match {
          case Some(string) => provide(string)
          case None => reject(MalformedRequestContentRejection("Request body form data lacked required `payload` field"))
        }
      }
      else {
        reject(ValidationRejection("Incorrect SHA-256 hash"))
      }
    }
  }
}

object TravisAuthDirectives extends TravisAuthDirectives

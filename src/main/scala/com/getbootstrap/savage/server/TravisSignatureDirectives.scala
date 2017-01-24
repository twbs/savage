package com.getbootstrap.savage.server

import java.util.Base64
import scala.util.Try
import akka.event.LoggingAdapter
import spray.http.FormData
import spray.routing.{Directive1, MalformedHeaderRejection, MalformedRequestContentRejection, ValidationRejection}
import spray.routing.directives.{BasicDirectives, HeaderDirectives, RouteDirectives, MarshallingDirectives}
import com.getbootstrap.savage.crypto.{RsaPublicKey, Sha1WithRsa, SuccessfullyVerified}
import com.getbootstrap.savage.util.Utf8String

trait TravisSignatureDirectives {
  import BasicDirectives.provide
  import HeaderDirectives.headerValueByName
  import RouteDirectives.reject
  import MarshallingDirectives.{entity, as}

  private val signatureHeaderName = "Signature"
  private val signatureHeaderValue = headerValueByName(signatureHeaderName)

  def travisSignature(log: LoggingAdapter): Directive1[Array[Byte]] = signatureHeaderValue.flatMap { base64 =>
    Try{ Base64.getDecoder.decode(base64) }.toOption match {
      case Some(bytesFromBase64) => provide(bytesFromBase64)
      case None => {
        log.error(s"Received Travis request with malformed Base64 value in ${signatureHeaderName} header!")
        reject(MalformedHeaderRejection(signatureHeaderName, "Malformed Base64 value"))
      }
    }
  }

  private val formDataEntity = entity(as[FormData])

  def stringEntityIfTravisSignatureValid(travisPublicKey: RsaPublicKey, log: LoggingAdapter): Directive1[String] = travisSignature(log).flatMap { signature =>
    formDataEntity.flatMap { formData =>
      formData.fields.toMap.get("payload") match {
        case Some(payload:String) => {
          Sha1WithRsa.verifySignature(signature = signature, publicKey = travisPublicKey, signedData = payload.utf8Bytes) match {
            case SuccessfullyVerified => provide(payload)
            case _ => {
              log.warning("Received Travis request with incorrect signature! Signature={} Payload={}", signature, payload)
              reject(ValidationRejection("Incorrect SHA-1+RSA signature"))
            }
          }
        }
        case None => {
          log.error("Received Travis request that was missing the `payload` field!")
          reject(MalformedRequestContentRejection("Request body form data lacked required `payload` field"))
        }
      }
    }
  }
}

object TravisSignatureDirectives extends TravisSignatureDirectives

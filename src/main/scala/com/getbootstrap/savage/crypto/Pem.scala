package com.getbootstrap.savage.crypto

import scala.util.{Try,Success,Failure}
import java.io.StringReader
import java.security.spec.X509EncodedKeySpec
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemObject


sealed class MalformedPemException(cause: Throwable) extends RuntimeException("The given data did not conform to the PEM format!", cause)

sealed class UnexpectedPemDataTypeException(expectedType: String, pemObj: PemObject)
  extends RuntimeException(s"PEM contained data of unexpected type! Expected: ${expectedType} Actual: ${pemObj.getType}")

// PEM is the name for the format that involves "-----BEGIN PUBLIC KEY-----" etc.
object Pem {
  private val PublicKeyPemType = "PUBLIC KEY"

  @throws[MalformedPemException]("if there is a problem decoding the PEM data")
  private def decode(pem: String): PemObject = {
    val pemReader = new PemReader(new StringReader(pem))
    val pemObjTry = Try { pemReader.readPemObject() }
    val closeTry = Try { pemReader.close() }
    (pemObjTry, closeTry) match {
      case (Failure(readExc), _) => throw new MalformedPemException(readExc)
      case (_, Failure(closeExc)) => throw new MalformedPemException(closeExc)
      case (Success(pemObj), Success(_)) => pemObj
    }
  }

  // Decodes PKCS8 data in PEM format into a X509EncodedKeySpec
  // which can be handled by sun.security.rsa.RSAKeyFactory
  @throws[UnexpectedPemDataTypeException]("if the PEM contains non-public-key data")
  def decodePublicKeyIntoSpec(publicKeyInPem: String): X509EncodedKeySpec = {
    val pemObj = decode(publicKeyInPem)
    pemObj.getType match {
      case PublicKeyPemType => new X509EncodedKeySpec(pemObj.getContent)
      case unexpectedType => throw new UnexpectedPemDataTypeException(PublicKeyPemType, pemObj)
    }
  }
}

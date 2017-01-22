package com.getbootstrap.savage.crypto

import java.security.Signature
import java.security.SignatureException
import java.security.InvalidKeyException


object Sha1WithRsa {
  private val signatureAlgorithmName = "SHA1withRSA" // Supported in all spec-compliant JVMs
  private def newSignatureVerifier(): Signature = Signature.getInstance(signatureAlgorithmName)

  def verifySignature(signature: Array[Byte], publicKey: RsaPublicKey, signedData: Array[Byte]): SignatureVerificationStatus = {
    val verifier = newSignatureVerifier()
    try {
      verifier.initVerify(publicKey.publicKey)
      verifier.update(signedData)
      verifier.verify(signature) match {
        case true => SuccessfullyVerified
        case false => FailedVerification
      }
    }
    catch {
      case keyExc:InvalidKeyException => ExceptionDuringVerification(keyExc)
      case sigExc:SignatureException => ExceptionDuringVerification(sigExc)
    }
  }
}

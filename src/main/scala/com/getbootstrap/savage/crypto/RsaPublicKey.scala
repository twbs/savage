package com.getbootstrap.savage.crypto

import scala.util.Try
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec


sealed case class RsaPublicKey private(publicKey: PublicKey)

object RsaPublicKey {
  private val rsaKeyFactory = KeyFactory.getInstance("RSA") // Supported in all spec-compliant JVMs

  def fromX509Spec(keySpec: X509EncodedKeySpec): Try[RsaPublicKey] = Try{ rsaKeyFactory.generatePublic(keySpec) }.map{ new RsaPublicKey(_) }
  def fromPem(pem: String): Try[RsaPublicKey] = Try{ Pem.decodePublicKeyIntoSpec(pem) }.flatMap{ fromX509Spec(_) }
}

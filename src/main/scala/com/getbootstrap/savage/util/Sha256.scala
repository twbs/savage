package com.getbootstrap.savage.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Sha256 {
  private val Sha256Algorithm = "SHA-256"
}

case class Sha256(hash: Array[Byte], plainText: Array[Byte]) {
  import Sha256.Sha256Algorithm

  @throws[NoSuchAlgorithmException]("if SHA-256 is not supported")
  private lazy val correct: Array[Byte] = {
    MessageDigest.getInstance(Sha256Algorithm).digest(plainText)
  }

  lazy val isValid: Boolean = MessageDigest.isEqual(hash, correct)

  def givenHex = hash.asHexBytes
  def correctHex = correct.asHexBytes
}

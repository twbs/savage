package com.getbootstrap.savage.util

import scala.util.Try

object IntFromStr {
  def unapply(str: String): Option[Int] = Try{ Integer.parseInt(str) }.toOption
}

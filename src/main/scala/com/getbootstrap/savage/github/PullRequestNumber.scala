package com.getbootstrap.savage.github

object PullRequestNumber {
  def apply(number: Int): Option[PullRequestNumber] = {
    if (number > 0) {
      Some(new PullRequestNumber(number))
    }
    else {
      None
    }
  }
  def unapply(number: Int): Option[PullRequestNumber] = PullRequestNumber(number)
}
class PullRequestNumber private(val number: Int) extends AnyVal {
  override def toString = s"PullRequestNumber(${number})"
}

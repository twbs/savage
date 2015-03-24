package com.getbootstrap.savage.travis.build_status

object BuildStatus {
  def apply(statusMessage: String): Option[BuildStatus] = {
    statusMessage match {
      case Pending.StatusMessage => Some(Pending)
      case Passed.StatusMessage => Some(Passed)
      case Fixed.StatusMessage => Some(Fixed)
      case Errored.StatusMessage => Some(Errored)
      case Failed.StatusMessage => Some(Failed)
      case Broken.StatusMessage => Some(Broken)
      case StillFailing.StatusMessage => Some(StillFailing)
      case _ => None
    }
  }
}
sealed trait BuildStatus {
  val StatusMessage: String
  def isSuccessful: Boolean
  override def toString: String = StatusMessage
}

// If the webhook is setup properly, we should never see a build with Pending status
object Pending extends BuildStatus {
  override val StatusMessage = "Pending"
  override val isSuccessful = false
}

sealed trait Succeeded extends BuildStatus {
  override val isSuccessful = true
}
sealed trait Failure extends BuildStatus {
  override val isSuccessful = false
}

object Passed extends Succeeded {
  override val StatusMessage = "Passed"
}
object Fixed extends Succeeded {
  override val StatusMessage = "Fixed"
}

object Errored extends Failure {
  override val StatusMessage = "Errored"
}
object Failed extends Failure {
  override val StatusMessage = "Failed"
}
object Broken extends Failure {
  override val StatusMessage = "Broken"
}
// Not mentioned in the Travis docs :-/
object StillFailing extends Failure {
  override val StatusMessage = "Still Failing"
}

package com.getbootstrap.savage.github.commit_status

import org.eclipse.egit.github.core.{CommitStatus => RawCommitStatus}

object Status {
  private val context = "continuous-integration/savage"
}
trait Status {
  def description: String
  protected def githubState: String
  def asRawStatus: RawCommitStatus = {
    // FIXME: set context too
    // FIXME: set targetUrl when available
    new RawCommitStatus().setState(githubState).setDescription(description + Status.context)
  }
  def name: String
}
case class Success(description: String) extends Status {
  override def githubState = RawCommitStatus.STATE_SUCCESS
  override def name = "Success"
}
case class Failure(description: String) extends Status {
  override def githubState = RawCommitStatus.STATE_FAILURE
  override def name = "Failure"
}
case class Error(description: String) extends Status {
  override def githubState = RawCommitStatus.STATE_ERROR
  override def name = "Error"
}
case class Pending(description: String) extends Status {
  override def githubState = RawCommitStatus.STATE_PENDING
  override def name = "Pending"
}

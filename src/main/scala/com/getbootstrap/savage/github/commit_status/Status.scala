package com.getbootstrap.savage.github.commit_status

import org.eclipse.egit.github.core.{CommitStatus => RawCommitStatus}

object Status {
  private val context = "savage"
}
trait Status {
  def description: String
  def url: Option[String]
  protected def githubState: String
  def asRawStatus: RawCommitStatus = {
    val status = new RawCommitStatus()
    status.setContext(Status.context).setState(githubState).setDescription(description)
    url.foreach{ status.setUrl(_) }
    status
  }
  def name: String
}
case class Success(description: String, buildUrl: String) extends Status {
  override def githubState = RawCommitStatus.STATE_SUCCESS
  override def name = "Success"
  override def url = Some(buildUrl)
}
case class Failure(description: String, buildUrl: String) extends Status {
  override def githubState = RawCommitStatus.STATE_FAILURE
  override def name = "Failure"
  override def url = Some(buildUrl)
}
case class Error(description: String) extends Status {
  override def githubState = RawCommitStatus.STATE_ERROR
  override def name = "Error"
  override def url = None
}
case class Pending(description: String) extends Status {
  override def githubState = RawCommitStatus.STATE_PENDING
  override def name = "Pending"
  override def url = None
}

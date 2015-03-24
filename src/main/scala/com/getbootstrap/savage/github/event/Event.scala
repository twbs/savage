package com.getbootstrap.savage.github.event

object Event {
  def apply(name: String): Option[Event] = {
    name match {
      case CommitComment.Name => Some(CommitComment)
      case Create.Name => Some(Create)
      case Delete.Name => Some(Delete)
      case Deployment.Name => Some(Deployment)
      case DeploymentStatus.Name => Some(DeploymentStatus)
      case Download.Name => Some(Download)
      case Follow.Name => Some(Follow)
      case Fork.Name => Some(Fork)
      case ForkApply.Name => Some(ForkApply)
      case Gist.Name => Some(Gist)
      case Gollum.Name => Some(Gollum)
      case IssueComment.Name => Some(IssueComment)
      case Issues.Name => Some(Issues)
      case Member.Name => Some(Member)
      case Membership.Name => Some(Membership)
      case PageBuild.Name => Some(PageBuild)
      case Ping.Name => Some(Ping)
      case Public.Name => Some(Public)
      case PullRequest.Name => Some(PullRequest)
      case PullRequestReviewComment.Name => Some(PullRequestReviewComment)
      case Push.Name => Some(Push)
      case Release.Name => Some(Release)
      case Repository.Name => Some(Repository)
      case Status.Name => Some(Status)
      case TeamAdd.Name => Some(TeamAdd)
      case Watch.Name => Some(Watch)
      case _ => None
    }
  }
}
sealed trait Event {
  def Name: String
}
object CommitComment extends Event {
  override val Name = "commit_comment"
}
object Create extends Event {
  override val Name = "create"
}
object Delete extends Event {
  override val Name = "delete"
}
object Deployment extends Event {
  override val Name = "deployment"
}
object DeploymentStatus extends Event {
  override val Name = "deployment_status"
}
object Download extends Event {
  override val Name = "download"
}
object Follow extends Event {
  override val Name = "follow"
}
object Fork extends Event {
  override val Name = "fork"
}
object ForkApply extends Event {
  override val Name = "fork_apply"
}
object Gist extends Event {
  override val Name = "gist"
}
object Gollum extends Event {
  override val Name = "gollum"
}
object IssueComment extends Event {
  override val Name = "issue_comment"
}
object Issues extends Event {
  override val Name = "issues"
}
object Member extends Event {
  override val Name = "member"
}
object Membership extends Event {
  override val Name = "membership"
}
object PageBuild extends Event {
  override val Name = "page_build"
}
object Ping extends Event {
  override val Name = "ping"
}
object Public extends Event {
  override val Name = "public"
}
object PullRequest extends Event {
  override val Name = "pull_request"
}
object PullRequestReviewComment extends Event {
  override val Name = "pull_request_review_comment"
}
object Push extends Event {
  override val Name = "push"
}
object Release extends Event {
  override val Name = "release"
}
object Repository extends Event {
  override val Name = "repository"
}
object Status extends Event {
  override val Name = "status"
}
object TeamAdd extends Event {
  override val Name = "team_add"
}
object Watch extends Event {
  override val Name = "watch"
}
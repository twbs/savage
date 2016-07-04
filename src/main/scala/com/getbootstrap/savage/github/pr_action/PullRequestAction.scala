package com.getbootstrap.savage.github.pr_action

object PullRequestAction {
  def apply(name: String): PullRequestAction = {
    name match {
      case Assigned.Name => Assigned
      case Unassigned.Name => Unassigned
      case Labeled.Name => Labeled
      case Unlabeled.Name => Unlabeled
      case Opened.Name => Opened
      case Closed.Name => Closed
      case Reopened.Name => Reopened
      case Synchronize.Name => Synchronize
      case Edited.Name => Edited
      case _ => Unknown(name)
    }
  }
}
sealed trait PullRequestAction {
  def Name: String
}
object Assigned extends PullRequestAction {
  override val Name = "assigned"
}
object Unassigned extends PullRequestAction {
  override val Name = "unassigned"
}
object Labeled extends PullRequestAction {
  override val Name = "labeled"
}
object Unlabeled extends PullRequestAction {
  override val Name = "unlabeled"
}
object Opened extends PullRequestAction {
  override val Name = "opened"
}
object Closed extends PullRequestAction {
  override val Name = "closed"
}
object Reopened extends PullRequestAction {
  override val Name = "reopened"
}
object Synchronize extends PullRequestAction {
  override val Name = "synchronize"
}
object Edited extends PullRequestAction {
  override val Name = "edited"
}
case class Unknown(Name: String) extends PullRequestAction

package com.getbootstrap.savage.github.pr_action

object PullRequestAction {
  def apply(name: String): Option[PullRequestAction] = {
    name match {
      case Assigned.Name => Some(Assigned)
      case Unassigned.Name => Some(Unassigned)
      case Labeled.Name => Some(Labeled)
      case Unlabeled.Name => Some(Unlabeled)
      case Opened.Name => Some(Opened)
      case Closed.Name => Some(Closed)
      case Reopened.Name => Some(Reopened)
      case Synchronize.Name => Some(Synchronize)
      case _ => None
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

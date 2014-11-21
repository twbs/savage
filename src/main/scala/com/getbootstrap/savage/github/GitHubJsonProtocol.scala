package com.getbootstrap.savage.github

import spray.json._
import org.eclipse.egit.github.core.RepositoryId

case class GitHubRepository(fullName: String) extends AnyVal {
  def id: RepositoryId = RepositoryId.createFromId(fullName)
}
case class GitHubUser(username: String) extends AnyVal
case class IssueOrComment(
  number: Option[Int], // issue number
  body: String,
  user: GitHubUser
)
case class IssueOrCommentEvent(
  repository: GitHubRepository,
  comment: Option[IssueOrComment],
  issue: IssueOrComment
) {
  def prNumber: Option[PullRequestNumber] = issue.number.flatMap{ PullRequestNumber(_) }
}

object GitHubJsonProtocol extends DefaultJsonProtocol {
  implicit object RepoJsonFormat extends JsonFormat[GitHubRepository] {
    override def write(repo: GitHubRepository) = JsObject("full_name" -> JsString(repo.fullName))
    override def read(value: JsValue) = {
      value.asJsObject.getFields("full_name") match {
        case Seq(JsString(fullName)) => new GitHubRepository(fullName)
        case _ => throw new DeserializationException("GitHubRepository expected")
      }
    }
  }
  implicit object UserFormat extends JsonFormat[GitHubUser] {
    override def write(user: GitHubUser) = JsObject("login" -> JsString(user.username))
    override def read(value: JsValue) = {
      value.asJsObject.getFields("login") match {
        case Seq(JsString(username)) => new GitHubUser(username)
      }
    }
  }
  implicit val issueOrCommentFormat = jsonFormat3(IssueOrComment.apply)
  implicit val issueOrCommentEventFormat = jsonFormat3(IssueOrCommentEvent.apply)
}

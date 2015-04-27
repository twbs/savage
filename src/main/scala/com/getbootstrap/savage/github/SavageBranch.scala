package com.getbootstrap.savage.github

import com.getbootstrap.savage.server.SettingsImpl
import com.getbootstrap.savage.util.{IntFromStr, PrefixedString}

object SavageBranch {
  def apply(branch: Branch)(implicit settings: SettingsImpl): Option[SavageBranch] = {
    branch.name.unprefix(settings.BranchPrefix).map{ _.split("-") } match {
      case Some(Array(IntFromStr(PullRequestNumber(num)), CommitSha(sha))) => Some(SavageBranch(num, sha))
      case _ => None
    }
  }
  def unapply(branch: Branch)(implicit settings: SettingsImpl): Option[(PullRequestNumber, CommitSha)] = SavageBranch(branch).map{ savBr => (savBr.prNum, savBr.commitSha) }
}
case class SavageBranch(prNum: PullRequestNumber, commitSha: CommitSha) {
  def branch(implicit settings: SettingsImpl): Branch = Branch(s"${settings.BranchPrefix}${prNum.number}-${commitSha.sha}").get
}

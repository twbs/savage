package com.getbootstrap.savage.github

object Branch {
  private val SafeBranchRegex = "^[0-9a-zA-Z_-]+$".r
  def apply(branchName: String): Option[Branch] = {
    branchName match {
      case SafeBranchRegex(_*) => Some(new Branch(branchName))
      case _ => None
    }
  }
  def unapply(branch: Branch): Option[String] = Some(branch.name)
}

class Branch private(val name: String) extends AnyVal {
  override def toString: String = s"Branch(${name})"
  def asRef = s"refs/heads/${name}"
}

package com.getbootstrap.savage.github.commit_status

import com.getbootstrap.savage.github.CommitSha

case class StatusForCommit(commit: CommitSha, status: Status)

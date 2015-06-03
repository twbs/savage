package com.getbootstrap.savage.github

import com.getbootstrap.savage.server.{Settings, ActorWithLogging}

abstract class GitHubActorWithLogging extends ActorWithLogging {
  protected implicit val settings = Settings(context.system)
  protected val gitHubClient = settings.github()
}

package com.getbootstrap.savage.server

import scala.concurrent.duration.FiniteDuration
import scala.collection.JavaConverters._
import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import akka.util.ByteString
import com.jcabi.github.Github
import com.jcabi.github.Coordinates.{Simple=>RepoId}
import com.getbootstrap.savage.github.Credentials
import com.getbootstrap.savage.http.{UserAgent=>UA}
import com.getbootstrap.savage.util.{FilePathWhitelist,FilePathWatchlist,Utf8String,RichConfig}

class SettingsImpl(config: Config) extends Extension {
  val MainRepoId: RepoId = new RepoId(config.getString("savage.github-repo-to-watch"))
  val TestRepoId: RepoId = new RepoId(config.getString("savage.github-test-repo"))
  val BotUsername: String = config.getString("savage.username")
  private val botPassword: String = config.getString("savage.password")
  private val botCredentials: Credentials = Credentials(username = BotUsername, password = botPassword)
  private val githubRateLimitThreshold: Int = config.getInt("savage.github-rate-limit-threshold")
  def github(): Github = botCredentials.github(githubRateLimitThreshold)(UserAgent)
  val GitHubWebHookSecretKey: ByteString = ByteString(config.getString("savage.github-web-hook-secret-key").utf8Bytes)
  val TravisToken: String = config.getString("savage.travis-token")
  val UserAgent: UA = UA(config.getString("spray.can.client.user-agent-header"))
  val DefaultPort: Int = config.getInt("savage.default-port")
  val SquelchInvalidHttpLogging: Boolean = config.getBoolean("savage.squelch-invalid-http-logging")
  val Whitelist: FilePathWhitelist = new FilePathWhitelist(config.getStringList("savage.whitelist").asScala)
  val Watchlist: FilePathWatchlist = new FilePathWatchlist(config.getStringList("savage.file-watchlist").asScala)
  val BranchPrefix: String = config.getString("savage.branch-prefix")
  val IgnoreBranchesFromMainRepo: Boolean = config.getBoolean("savage.ignore-branches-from-watched-repo")
  val TrustedOrganizations: Set[String] = config.getStringList("savage.trusted-orgs").asScala.toSet
  val SetCommitStatus: Boolean = config.getBoolean("savage.set-commit-status")
  val TravisTimeout: FiniteDuration = config.getFiniteDuration("savage.travis-timeout")
}
object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {
  override def lookup() = Settings
  override def createExtension(system: ExtendedActorSystem) = new SettingsImpl(system.settings.config)
  override def get(system: ActorSystem): SettingsImpl = super.get(system)
}

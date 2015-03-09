package com.getbootstrap.savage.server

import scala.util.{Success,Failure}
import scala.concurrent.duration._
import scala.util.Try
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.routing.SmallestMailboxPool
import akka.util.Timeout


object Boot extends App {
  val arguments = args.toSeq
  val argsPort = arguments match {
    case Seq(portStr: String) => {
      Try{ portStr.toInt } match {
        case Failure(_) => {
          System.err.println("USAGE: savage <port-number>")
          System.exit(1)
          None // dead code
        }
        case Success(portNum) => Some(portNum)
      }
    }
    case Seq() => None
  }

  run(argsPort)

  def squelchInvalidHttpLogging() {
    import org.slf4j.LoggerFactory
    import ch.qos.logback.classic.{Logger,Level}

    LoggerFactory.getLogger("spray.can.server.HttpServerConnection").asInstanceOf[Logger].setLevel(Level.ERROR)
  }

  def run(port: Option[Int]) {
    implicit val system = ActorSystem("on-spray-can")
    val settings = Settings(system)
    // import actorSystem.dispatcher

    if (settings.SquelchInvalidHttpLogging) {
      squelchInvalidHttpLogging()
    }

    val deleter = system.actorOf(SmallestMailboxPool(3).props(Props(classOf[BranchDeleter])), "branch-deleters")
    val statusSetters = system.actorOf(SmallestMailboxPool(3).props(Props(classOf[CommitStatusSetter])), "status-setters")
    val commenter = system.actorOf(SmallestMailboxPool(3).props(Props(classOf[PullRequestCommenter])), "gh-pr-commenters")
    val pusher = system.actorOf(Props(classOf[PullRequestPusher]), "pr-pusher")
    val prHandlers = system.actorOf(SmallestMailboxPool(3).props(Props(classOf[PullRequestEventHandler], pusher, statusSetters)), "pr-handlers")
    val webService = system.actorOf(Props(classOf[SavageWebService], prHandlers, commenter, deleter, statusSetters), "savage-service")

    implicit val timeout = Timeout(15.seconds)
    IO(Http) ? Http.Bind(webService, interface = "0.0.0.0", port = port.getOrElse(settings.DefaultPort))
  }
}

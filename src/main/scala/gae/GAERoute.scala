package gae

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

/**
  * GAEで利用されるルーティングを定義
  */
trait GAERoute {
  def gaeRoutes(implicit actorSystem: ActorSystem): Route = GAERoute.routes
}

object GAERoute {

  import GAERouteOps._

  def routes(implicit actorSystem: ActorSystem): Route = healthRoute() ~ startRoute ~ stopRoute
}

object GAERouteOps {

  def healthRoute(logic: Route = complete("")): Route = {
    pathPrefix("_ah") {
      path("health") {
        get {
          logic
        }
      }
    }
  }

  def startRoute(implicit actorSystem: ActorSystem): Route = {
    val logger = Logging(actorSystem, getClass)
    pathPrefix("_ah") {
      path("start") {
        get {
          logger.info("/_ah/start")
          complete("")
        }
      }
    }
  }

  def stopRoute(implicit actorSystem: ActorSystem): Route = {
    val logger = Logging(actorSystem, getClass)
    pathPrefix("_ah") {
      path("stop") {
        get {
          logger.info("/_ah/stop")
          implicit val ec: ExecutionContext = actorSystem.dispatcher
          actorSystem.terminate().foreach { _ =>
            logger.info("terminate")
          }
          complete("")
        }
      }
    }
  }

}

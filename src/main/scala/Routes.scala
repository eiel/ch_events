import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import chatwork.ChatWork
import chatwork.ChatWork._
import orunka.adapter.ChatWorkPublisher

import scala.concurrent.ExecutionContext

trait Routes {
  def routes(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) = {
    import orunka._
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    val app = OrunkaApplication.create()

    for {
      token <- getChatWorkAPITokenFromEnv
      roomId <- sys.env.get("ROOM_ID").map(_.toInt)
    } {
      app.subscribe(ChatWorkPublisher.props(roomId, ChatWork(token)))
    }
    healthRoute ~ // GET /_ah/health
      pathSingleSlash {
        get {
          // GET /
          index()
        }
      } ~
      pathPrefix("orunka") { // GET /orunka
        OrunkaRoute(app)
      }
  }

  private val healthRoute: Route = {
    pathPrefix("_ah") {
      path("health") {
        get {
          complete("")
        }
      }
    }
  }

  private def index() = complete(
    HttpResponse(
      entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        "Welcome to eiel-app"
      )
    )
  )
}

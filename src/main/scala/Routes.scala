import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import chatwork.ChatWork
import com.typesafe.config.ConfigFactory
import gae.GAERoute
import orunka.adapter.ChatWorkPublisher

import scala.concurrent.ExecutionContext

trait Routes extends GAERoute {
  def orunkaApp(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer): Route = {
    import orunka._
    implicit val ec: ExecutionContext = actorSystem.dispatcher

    val app = OrunkaApplication.create()

    val config = ConfigFactory.load()
    val token = ChatWork.APIToken(config.getString("chatwork.token"))
    val orunkaConfig = ConfigFactory.parseURL(new URL(config.getString("orunka.configURI")))
    val roomId = orunkaConfig.getInt("orunka.roomId")
    app.subscribe(ChatWorkPublisher.props(roomId, ChatWork(token), orunkaConfig))

    OrunkaRoute(app)
  }

  def routes(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) = {
    val app = orunkaApp
    gaeRoutes ~
      pathSingleSlash {
        get {
          // GET /
          index()
        }
      } ~
      pathPrefix("orunka") {
        // GET /orunka
        app
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

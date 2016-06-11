import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import chatwork.ChatWork
import chatwork.ChatWork._
import com.typesafe.config.{Config, ConfigFactory}
import gae.GAERoute
import orunka.adapter.ChatWorkPublisher

import scala.concurrent.ExecutionContext

trait Routes extends GAERoute {
  def routes(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) = {
    import orunka._
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    val app = OrunkaApplication.create()

    for {
      token <- getChatWorkAPITokenFromEnv
    } {
      val config: Config = sys.env.get("ORUNKA_CONF_URI").fold {
        ConfigFactory.parseFile(new java.io.File("orunka.conf"))
      } { uri =>
        ConfigFactory.parseURL(new URL(uri))
      }
      val roomId = config.getInt("orunka.roomId")
      app.subscribe(ChatWorkPublisher.props(roomId, ChatWork(token), config))
    }

    gaeRoutes ~
      pathSingleSlash {
        get {
          // GET /
          index()
        }
      } ~
      pathPrefix("orunka") {
        // GET /orunka
        OrunkaRoute(app)
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

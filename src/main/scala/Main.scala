import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

object Main extends App with Routes {
  implicit val actorSystem: ActorSystem = ActorSystem("http")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val config: Config = ConfigFactory.load()
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")
  val logger = Logging(actorSystem, getClass)
  val binding = Http().bindAndHandle(routes, interface, port).map { (s: ServerBinding) =>
    logger.info(s"start server $interface:$port")
  }
}

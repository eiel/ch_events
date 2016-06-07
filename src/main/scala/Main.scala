import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

object Main extends App with Routes {
  implicit val actorSystem: ActorSystem = ActorSystem("http")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val interface = "0.0.0.0"
  val port = getPort.getOrElse(8080)
  val logger = Logging(actorSystem, getClass)
  val binding = Http().bindAndHandle(routes, interface, port).map { (s: ServerBinding) =>
    logger.info(s"start server $interface:$port")
  }

  def getPort: Option[Int] = sys.env.get("PORT").map(_.toInt)
}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Main extends App with Routes {
  implicit val actorSystem: ActorSystem = ActorSystem("http")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val interface = "0.0.0.0"
  val port = getPort.getOrElse(8080)
  println(s"start server $interface:$port")
  val binding = Http().bindAndHandle(routes, interface, port)

  def getPort: Option[Int] = sys.env.get("PORT").map(_.toInt)
}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Main extends App with Routes {
  implicit val actorSystem: ActorSystem = ActorSystem("http")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val interface = "localhost"
  val port = 8081
  println(s"start server $interface:$port")
  val binding = Http().bindAndHandle(routes, interface, port)
}

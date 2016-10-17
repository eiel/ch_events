import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._

trait Routes {
  val routes = pathSingleSlash {
    get {
      // GET localhost:8080
      index()
    }
  }

  private def index() = complete(
    HttpResponse(
      entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        "Welcome to akka-http"
      )
    )
  )
}

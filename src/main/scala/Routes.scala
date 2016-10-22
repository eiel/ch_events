import akka.http.javadsl.server.Directives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route

trait Routes {
  private val healthRoute: Route = {
    pathPrefix("_ah") {
      path("health") {
        get {
          complete("R")
        }
      }
    }
  }

  val routes = pathSingleSlash {
    get {
      // GET localhost:8080
      index()
    }
  } ~ healthRoute

  private def index() = complete(
    HttpResponse(
      entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        "Welcome to akka-http"
      )
    )
  )
}

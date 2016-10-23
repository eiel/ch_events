package orunka

import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext
import scala.util.Success

object OrunkaRoute {
  import akka.http.scaladsl.server.Directives._

  def apply(app: OrunkaApplication)(implicit executionContext: ExecutionContext): Route = createRoutes(app, executionContext)

  def createRoutes(implicit app: OrunkaApplication, ec: ExecutionContext): Route = {
    pathPrefix ("members") {
      pathEndOrSingleSlash {
        get {
          indexMembers
        }
      } ~
        path(Remaining) { (param: String) =>
          post {
            putMembers(param)
          }
        }
    }
  }

  def indexMembers(implicit app: OrunkaApplication, ec: ExecutionContext): Route = {
    val result = app.members().map(_.mkString("\n"))
    onComplete(result) {
      case Success(value) => complete(value)
      case _ => complete("fail")
    }
  }

  def putMembers(request: String)(implicit app: OrunkaApplication): Route = {
    val members = request.split(",").toSet
    app.setMembers(members)
    complete("Success")
  }
}


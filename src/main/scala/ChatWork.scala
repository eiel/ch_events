import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import scala.collection.immutable

class ChatWork {

}

object ChatWork {
  def chatWorkTokenHeader(token: String): HttpHeader = RawHeader("X-ChatWorkToken", token)

  def queryMessages(roomId: Int, token: String): Unit = {
    val request = requestMessages(roomId, token)
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    Http().singleRequest(request)
  }

  def requestMessages(roomId: Int, token: String): HttpRequest = {
    HttpRequest(
      uri = s"https://api.chatwork.com/v1/rooms/$roomId/messages",
      headers = immutable.Seq(chatWorkTokenHeader(token))
    )
  }
}

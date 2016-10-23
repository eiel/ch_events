package chatwork

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, JsonFraming, Sink, Source}
import akka.util.ByteString
import spray.json._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ChatWork {

}

object ChatWork {
  case class APIToken(token: String)

  def getChatWorkAPITokenFromEnv: Option[APIToken] = {
    sys.env.get("CHATWORK_API_TOKEN").map(APIToken.apply)
  }

  case class AccountJson(account_id: Long, name: String, avatar_image_url: ImageUrl) {
    def toAccount: Account = Account(AccountId(account_id), name, avatar_image_url)
  }
  case class MessageJson(message_id: Long, account: AccountJson, body: ChatWorkText, send_time: DateTime, updateTime: Option[DateTime]) {
    def toMessage: Message = Message(MessageId(message_id), account.toAccount, body, send_time, updateTime)
  }

  trait _JsonFormat extends DefaultJsonProtocol {
    implicit val dateTimeFormat = new JsonFormat[DateTime] {
      override def write(obj: DateTime): JsValue = obj.clicks.toJson

      override def read(json: JsValue): DateTime = DateTime(json.convertTo[Long])
    }

    implicit val accountFormat: JsonFormat[AccountJson] = jsonFormat3(AccountJson.apply)

    implicit val messageFormat: JsonFormat[MessageJson] = jsonFormat5(MessageJson.apply)
  }

  type ChatWorkText = String
  type ImageUrl = String
  case class MessageId(id: Long)
  case class AccountId(id: Long)
  case class Account(accountId: AccountId, name: String, avatarImageUrl: ImageUrl)
  case class Message(messageId: MessageId, account: Account, body: ChatWorkText, sendTime: DateTime, updateTime: Option[DateTime])

  def chatWorkTokenHeader(token: String): HttpHeader = RawHeader("X-ChatWorkToken", token)

  def queryMessages(roomId: Int, token: APIToken)
                   (implicit actorSystem: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Source[Message, Any]] = {
    val n = new _JsonFormat {}
    import n._

    def requestMessages(roomId: Int, token: APIToken): HttpRequest = {
      HttpRequest(
        uri = s"/v1/rooms/$roomId/messages",
        headers = immutable.Seq(chatWorkTokenHeader(token.token))
      )
    }

    val request: HttpRequest = requestMessages(roomId, token)
    val jsonFraming: Flow[ByteString, ByteString, NotUsed] = JsonFraming.objectScanner(100000)

    def responseToMessage(response: HttpResponse): Source[Message, Any] = {
      response.entity.dataBytes.via(jsonFraming).map { bstr =>
        val str = bstr.decodeString("UTF-8")
        str.parseJson.convertTo[MessageJson].toMessage
      }
    }

    val connectionPoolFlow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), HostConnectionPool] = Http().cachedHostConnectionPoolHttps[Int]("api.chatwork.com")

    Source.single(request -> 42)
      .via(connectionPoolFlow)
      .runWith(Sink.head)
      .flatMap { case (tryResponse, _) =>
        Future.fromTry(tryResponse.map(responseToMessage))
      }
  }

  def commandCreateMessage(text: String, roomId: Int, token: APIToken)
    (implicit actorSystem: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[(Try[HttpResponse], Int)] = {

    def requestCreateMessage(message: String, roomId: Int, token: APIToken): HttpRequest = {
      HttpRequest(
        method = HttpMethods.POST,
        uri = s"/v1/rooms/$roomId/messages",
        headers = immutable.Seq(chatWorkTokenHeader(token.token)),
        entity = FormData("body" -> message).toEntity
      )
    }

    val connectionPoolFlow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), HostConnectionPool] = Http().cachedHostConnectionPoolHttps[Int]("api.chatwork.com")
    val request = requestCreateMessage(text, roomId, token)
    println(request)
    Source.single(request -> 42)
      .via(connectionPoolFlow)
      .runWith(Sink.head)
  }
}

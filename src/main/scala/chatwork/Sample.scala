package chatwork

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Sample {
  def run(): Unit = {
    import ChatWork._
    for {
      token <- getChatWorkAPITokenFromEnv
      roomId <- sys.env.get("ROOM_ID").map(_.toInt)
    } {
      implicit val actorSystem = ActorSystem()
      implicit val materializer = ActorMaterializer()
      implicit val ec: ExecutionContext = actorSystem.dispatcher

        commandCreateMessage("hoge", roomId, token).onComplete {
          case Success(v) =>
            println(v)
            queryMessages(roomId, token).map { source =>
              source.runForeach(println)
            }
          case Failure(ex) =>
            println(ex)
        }
    }
  }
}

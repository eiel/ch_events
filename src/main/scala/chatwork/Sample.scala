package chatwork

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Sample {
  def run(): Unit = {
    import ChatWork._
    for {
      token <- getChatWorkAPITokenFromEnv
      roomId <- sys.env.get("ROOM_ID")
    } {
      implicit val actorSystem = ActorSystem()
      implicit val materializer = ActorMaterializer()
      import scala.concurrent.ExecutionContext.Implicits.global
      queryMessages(roomId.toInt, token).map { source =>
        source.runForeach(println)
      }
    }
  }
}

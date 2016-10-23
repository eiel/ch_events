package orunka

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

object Orunka {
  def flow(store: ActorRef)(implicit executionContext: ExecutionContext): Flow[String, Future[String], Any] = {
    Flow.apply.filter { str: String =>
      str.matches(""".*おるんか.*""")
    }.map { _ =>
      import Store.GetMembers
      implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.MINUTES))
      val futureMembers: Future[Seq[String]] = ask(store, GetMembers).mapTo[Seq[String]]
      futureMembers.map { n =>

        createMessage(n)
      }
    }
  }

  def createMessage(members: Seq[String]): String = {
    if (members.isEmpty) {
      "だれもおらんよ"
    } else {
      members.foldLeft[StringBuilder](StringBuilder.newBuilder) {
        case (acc, str) => acc.append(s" $str")
      }.append(" がおるんよ").mkString
    }
  }
}

object Store {
  type Member = String

  case class SetMemebers(members: Seq[Member])

  case object GetMembers

  object StoreActor {
      def props(): Props = {
         Props[StoreActor]
      }
  }

  class StoreActor extends Actor {
    var members: Seq[Member] = Seq()

    override def receive: Receive = {
      case SetMemebers(m) =>
        members = m
      case GetMembers =>
        context.sender() ! members
    }
  }
}

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

object MemberEventPublisherActor {

  case class Subscribe(props: Props)

  sealed trait Event

  case class AddEvent(member: String) extends Event

  case class RemoveEvent(member: String) extends Event

  def props(): Props = {
    Props[MemberEventPublisherActor]
  }
}

class MemberEventPublisherActor extends Actor {

  override def receive: Receive = {
    case MemberEventPublisherActor.Subscribe(props: Props) =>
      context.actorOf(props)
    case m: MemberEventPublisherActor.Event =>
      context.children.foreach(_ ! m)
  }
}

object OrunkaActor {

  case class SetMembers(members: Set[String])

  case class GetMembers()

  case class Subscribe(props: Props)

  def props() = Props[OrunkaActor]
}

class OrunkaActor extends Actor {

  def store = context.child("store")
  def publisher = context.child("publisher")

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    context.actorOf(Store.StoreActor.props(), "store")
    context.actorOf(MemberEventPublisherActor.props(), "publisher")
    super.preStart()
  }

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.MINUTES))
  implicit val ec: ExecutionContext= context.dispatcher

  override def receive: Receive = {
    case OrunkaActor.SetMembers(members) =>
      store.foreach { s =>
        ask(s, Store.GetMembers).mapTo[Set[String]].map { lastMembers =>
          val addMembers = members diff lastMembers
          addMembers.foreach(m => publisher.foreach {
            println(s"AddMemberEvent: $m")
            _ ! MemberEventPublisherActor.AddEvent(m)
          })
          val removeMembers = lastMembers diff members
          removeMembers.foreach(m => publisher.foreach {
            println(s"RemoveMemberEvent: $m")
            _ ! MemberEventPublisherActor.RemoveEvent(m)
          })
          s ! Store.SetMembers(members)
        }
      }
    case OrunkaActor.GetMembers =>
      val sender = context.sender()
      store.foreach { s =>
        ask(s, Store.GetMembers).mapTo[Set[String]].map(sender ! _)
      }
    case OrunkaActor.Subscribe(props) =>
      publisher.foreach(_ ! MemberEventPublisherActor.Subscribe(props))
  }
}

object Store {
  type Member = String

  case class SetMembers(members: Set[Member])

  case object GetMembers

  object StoreActor {
    def props(): Props = {
      Props[StoreActor]
    }
  }

  class StoreActor extends Actor {
    var members: Set[Member] = Set()

    override def receive: Receive = {
      case SetMembers(m) =>
        members = m
      case GetMembers =>
        context.sender() ! members
    }
  }

}

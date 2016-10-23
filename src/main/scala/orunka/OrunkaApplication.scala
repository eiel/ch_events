package orunka

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait OrunkaApplication {
  trait Event
  def members(): Future[Set[String]]

  def setMembers(members: Set[String]): Unit

  def subscribe(props: Props): Unit
}

object OrunkaApplication {
  def apply(orunka: ActorRef): OrunkaApplication = new OrunkaApplicationImpl(orunka)

  def create()(implicit actorSystem: ActorSystem): OrunkaApplication = {
    val store = actorSystem.actorOf(OrunkaActor.props(), "orunka")
    OrunkaApplication(store)
  }
}

class OrunkaApplicationImpl(val orunka: ActorRef) extends OrunkaApplication {

  import OrunkaActor._

  override def members(): Future[Set[String]] = {
    import akka.pattern.ask
    implicit val timeout = Timeout(FiniteDuration(10, TimeUnit.SECONDS))
    ask(orunka, GetMembers).mapTo[Set[String]]
  }

  override def setMembers(members: Set[String]): Unit = {
    orunka ! SetMembers(members)
  }

  override def subscribe(props: Props): Unit = {
    orunka ! Subscribe(props: Props)
  }
}


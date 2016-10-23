package orunka

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait OrunkaApplication {
  def members(): Future[Seq[String]]
  def setMembers(members: Seq[String]): Unit
}

object OrunkaApplication {
  def apply(store: ActorRef): OrunkaApplication = new OrunkaApplicationImpl(store)

  def create()(implicit actorSystem: ActorSystem): OrunkaApplication = {
    val store = actorSystem.actorOf(Store.StoreActor.props())
    OrunkaApplication(store)
  }
}

class OrunkaApplicationImpl(val store: ActorRef) extends OrunkaApplication {
  import Store._
  def members(): Future[Seq[String]] = {
    import akka.pattern.ask
    implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.MINUTES))
    ask(store, GetMembers).mapTo[Seq[String]]
  }

  override def setMembers(members: Seq[String]): Unit = {
    store ! SetMemebers(members)
  }
}


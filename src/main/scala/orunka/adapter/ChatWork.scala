package orunka.adapter

import akka.actor.{Actor, Props}
import chatwork.ChatWork
import orunka.MemberEventPublisherActor._

object LogSubscriber {
  def props: Props = Props[LogSubscriber]
}

class LogSubscriber extends Actor {
  override def receive: Receive = {
    case AddEvent(member) =>
      println(s"${member}が入室しました")
    case RemoveEvent(member) =>
      println(s"${member}が退室しました")
  }
}

object ChatWorkPublisher {
  def props(roomId: Int, chatwork: ChatWork): Props = Props(classOf[ChatWorkPublisher], roomId, chatwork)
}

class ChatWorkPublisher(val roomId: Int, chatwork: ChatWork) extends Actor {
  override def receive: Receive = {
    case AddEvent(member) =>
      chatwork.commandCreateMessage(s"${member}が入室しました", roomId)
    case RemoveEvent(member) =>
      chatwork.commandCreateMessage(s"${member}が退室しました", roomId)
  }
}

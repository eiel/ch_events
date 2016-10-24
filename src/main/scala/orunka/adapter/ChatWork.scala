package orunka.adapter

import akka.actor.{Actor, Props}
import chatwork.ChatWork
import com.typesafe.config.Config
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
  def props(roomId: Int, chatwork: ChatWork, config: Config): Props = Props(classOf[ChatWorkPublisher], roomId, chatwork, config)
}

class ChatWorkPublisher(val roomId: Int, chatwork: ChatWork, config: Config) extends Actor {
  override def receive: Receive = {
    case AddEvent(member) =>
      getName(member).foreach { name =>
        chatwork.commandCreateMessage(makeInfo("おるんか", s"$name さんが入室しました:)"), roomId)
      }
    case RemoveEvent(member) =>
      getName(member).foreach { name =>
        chatwork.commandCreateMessage(makeInfo("おるんか", s"$name さんが退室しました;("), roomId)
      }
  }

  def makeInfo(title: String, text: String): String = {
    s"[info][title]$title[/title]$text[/info]"
  }

  def getName(member: String): Option[String] = {
    val path = member.replaceAll(":", "")
    val abs_path = s"orunka.macs.$path"
    val aid: Option[Int] = if (config.hasPath(abs_path)) {
      Some(config.getInt(abs_path))
    } else {
      // TODO ログ
      None
    }
    aid.map { aid =>
      s"[piconname:$aid]"
    }
  }
}

package chatwork.endpoint

import akka.http.scaladsl.model.Uri
import chatwork.model.{FileId, MessageId, RoomId, TaskId}

object Endpoint {
  object V1 {
    val version = "v1"
    val basePath: Uri.Path = Uri.Path / version
    val mePath: Uri.Path = basePath / "me"
    val myPath: Uri.Path = basePath / "my"
    val myStatusPath: Uri.Path = myPath / "status"
    val myTasksPath: Uri.Path = myPath / "tasks"
    val contactsPath: Uri.Path = basePath / "contacts"
    val roomsPath: Uri.Path = basePath / "rooms"
    def roomPath(implicit roomId: RoomId): Uri.Path = roomsPath / roomId.id.toString
    def messagesPath(implicit roomId: RoomId): Uri.Path = roomPath / "messages"
    def messagePath(implicit roomId: RoomId, messageId: MessageId): Uri.Path = messagesPath / messageId.id.toString
    def membersPath(implicit roomId: RoomId): Uri.Path = roomPath / "members"
    def tasksPath(implicit roomId: RoomId): Uri.Path = roomPath / "tasks"
    def taskPath(implicit roomId: RoomId, taskId: TaskId): Uri.Path = tasksPath / taskId.id.toString
    def filesPath(implicit roomId: RoomId): Uri.Path = roomPath / "files"
    def filePath(implicit roomId: RoomId, fileId: FileId): Uri.Path = filesPath / fileId.id.toString
  }
}

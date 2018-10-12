package actors.convert_flow

import actors.AdminActor.GetUserCount
import actors.messages.ActorFailed
import akka.NotUsed
import akka.stream.scaladsl.Flow
import play.api.libs.json.{JsValue, Json, OFormat}

object AdminRequestConvertFlow extends SocketRequestConvertFlow {
  final case class AdminRequest(
      action: String
  )
  implicit val adminRequestConvertFlow: OFormat[AdminRequest] = Json.format[AdminRequest]

  final val authenticate = "auth"

  def jsonToCompilerCommand(msg: JsValue): Any = {
    Json.fromJson[AdminRequest](msg).asOpt match {
      case Some(AdminRequest(action)) if action == "user-count" => GetUserCount()
      case _ => ActorFailed("Bad json input")
    }
  }

  override def apply(): Flow[JsValue, Any, NotUsed] = {
    Flow[JsValue].map(jsonToCompilerCommand)
  }
}

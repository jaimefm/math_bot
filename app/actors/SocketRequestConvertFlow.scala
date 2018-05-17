package actors

import actors.messages.{ ActorFailed, CompilerExecute, CompilerHalt, SocketRequest }
import akka.NotUsed
import akka.stream.scaladsl.Flow
import model.models.Problem
import play.api.libs.json._

object SocketRequestConvertFlow {

  implicit val sockRequestReads : Reads[SocketRequest] = Json.reads[SocketRequest]

  def jsonToCompilerCommand(msg : JsValue) : Any = {
    println(msg.toString())
    Json.fromJson[SocketRequest](msg).asOpt match {
      case Some(SocketRequest(_, _, Some(true))) => CompilerHalt()
      case Some(SocketRequest(Some(steps), Some(problem), _)) => CompilerExecute(steps, Problem(encryptedProblem = problem))
      case _ => ActorFailed("Invalid socket request json.")
    }
  }

  def apply() : Flow[JsValue, Any, NotUsed]  = {
    Flow[JsValue].map(jsonToCompilerCommand)
  }
}

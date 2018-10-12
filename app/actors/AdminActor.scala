package actors
import actors.messages.ActorFailed
import akka.actor.{Actor, ActorRef, Props}
import com.google.inject.Inject
import daos.PlayerTokenDAO
import loggers.MathBotLogger
import play.api.Environment
import play.api.libs.ws.WSClient

object AdminActor {
  case class GetUserCount()
  case class UserCount(count: String)

  def props(out: ActorRef, playerTokenDAO: PlayerTokenDAO, ws: WSClient, environment: Environment) =
    Props(new AdminActor(out, playerTokenDAO, ws, environment))
}

class AdminActor @Inject()(out: ActorRef, playerTokenDAO: PlayerTokenDAO, ws: WSClient, environment: Environment)
    extends Actor {
  import AdminActor._
  import context.dispatcher

  private final val className = "AdminActor"

  override def receive: Receive = {
    case GetUserCount() =>
      playerTokenDAO.getTokenCount
        .map { count =>
          out ! UserCount(count)
        }
    case actorFailed: ActorFailed => out ! actorFailed
  }
}

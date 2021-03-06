package controllers

import actors.GameStatsActor
import actors.GameStatsActor.GetTokenCount
import actors.messages._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import javax.inject.Singleton
import loggers.MathBotLogger
import daos.PlayerTokenDAO
import play.api.Environment
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class GameStatsController @Inject()(system: ActorSystem,
                                    playerTokenDAO: PlayerTokenDAO,
                                    logger: MathBotLogger,
                                    environment: Environment)
    extends Controller {
  implicit val timeout: Timeout = 5000.minutes

  val gameStatsActor =
    system.actorOf(GameStatsActor.props(playerTokenDAO, logger, environment), "game-stats-actor")

  def getCount = Action.async { implicit request =>
    (gameStatsActor ? GetTokenCount).mapTo[Either[String, ActorFailed]].map {
      case Left(count) =>
        Ok(s"User token count: $count")
      case Right(actorFailed) => BadRequest(actorFailed.msg)
    }
  }
}

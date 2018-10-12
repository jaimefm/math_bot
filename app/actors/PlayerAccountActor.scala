package actors

import java.time.Instant
import java.util.Date

import actors.messages.playeraccount._
import akka.actor.Actor
import daos.PlayerAccountDAO
import javax.inject.Inject
import models.PlayerAccount

import scala.concurrent.ExecutionContext

class PlayerAccountActor @Inject()(
    val playerAccount: PlayerAccountDAO
)(implicit val ec : ExecutionContext) extends Actor {
  override def receive: Receive = {
    case CreateAccount(jwt) => {
      val now = Date.from(Instant.now())
      val s = sender()

      playerAccount.find(jwt.playerTokenId) map {
        case Some(pa) =>
          playerAccount.updateAccess(pa.tokenId)
          s ! AccountCreated(pa.tokenId)
        case None =>
          val pa = PlayerAccount(jwt.playerTokenId, jwt.iss, jwt.sub, jwt.email, jwt.name, now, now, 1, "", "", jwt.picture)
          playerAccount.put(pa) foreach {
            _ => s ! AccountCreated(pa.tokenId)
          }
      }

    }
    case UpdateAccountAccess(idToken) =>
      playerAccount.updateAccess(idToken)

    case UpdateMaxLevelAndStep(idToken, maxLevel, maxStep) =>
      playerAccount.updateMaxLevelAndStep(idToken, maxLevel, maxStep)
  }
}

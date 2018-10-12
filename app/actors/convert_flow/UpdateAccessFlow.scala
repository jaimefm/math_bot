package actors.convert_flow

import actors.messages.playeraccount.UpdateAccountAccess
import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.Sink
import types.TokenId

object UpdateAccessFlow {

  def apply(tokenId : TokenId, playerAccountActor: ActorRef) : Sink[Any, NotUsed] = Sink.onComplete {
    _ =>
      playerAccountActor ! UpdateAccountAccess(tokenId)
  }
}

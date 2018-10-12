package actors.messages.playeraccount

import types.TokenId

case class UpdateMaxLevelAndStep(tokenId : TokenId, level : String, step : String)

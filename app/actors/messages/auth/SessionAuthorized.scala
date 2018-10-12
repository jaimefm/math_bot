package actors.messages.auth

import actors.messages.MessageAction
import play.api.libs.json.{Json, OFormat, Reads}
import utils.SecureIdentifier

case class SessionAuthorized(sessionId: SecureIdentifier,
                             name: String,
                             picture: String,
                             sub: String,
                             email: String,
                             action: Symbol = 'sessionAuthorized)
    extends MessageAction

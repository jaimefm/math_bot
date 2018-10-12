package actors.messages.auth

import actors.messages.MessageAction

case class Auth0MigrateNeeded(email : String, user_id : String, action : Symbol = 'auth0MigrateNeeded) extends MessageAction


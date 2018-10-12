package actors.messages

import play.api.libs.json.Json

object Auth0Authorized {
  implicit val auth0SuccessResponseFormat = Json.format[Auth0Authorized]
}

case class Auth0Authorized(access_token: String, id_token: String, scope: String, expires_in: Int, token_type: String)

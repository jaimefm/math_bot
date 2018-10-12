package models

import play.api.libs.json.{ Json, OFormat }

case class Auth0Migrate(jwt : String, password: String)

object Auth0Migrate {

  implicit val auth0MigrateFormat: OFormat[Auth0Migrate] = Json.format[Auth0Migrate]

}
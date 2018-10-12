package models
import play.api.libs.json.{Json, OFormat}

object PasswordRecovery {
  implicit val passwordRecoveryFormat: OFormat[PasswordRecovery] = Json.format[PasswordRecovery]
}

case class PasswordRecovery(email: String)

package models
import play.api.libs.json._

object UsernameAndPassword {
  implicit val usernameAndPasswordFormat: OFormat[UsernameAndPassword] = Json.format[UsernameAndPassword]
}

case class UsernameAndPassword(username: String, password: String)

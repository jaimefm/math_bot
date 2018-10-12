package models
import play.api.libs.json.{Json, OFormat}

object SignUpForm {
  implicit val signUpFormFormat: OFormat[SignUpForm] = Json.format[SignUpForm]
}

case class SignUpForm(username: String, name: String, picture: Option[String] = None, password: String)

package actors.messages.auth

import models.UsernameAndPassword
import play.api.libs.json._

object AuthFormatters {

  implicit val symbolFormatter : Format[Symbol] = new Format[Symbol] {
    override def writes(o : Symbol) : JsValue = JsString(o.name)

    override def reads(json : JsValue) : JsResult[Symbol] = json match {
      case JsString(name) => JsSuccess(Symbol(name))
      case _ => JsError("Unable to read Symbol value.")
    }
  }

  implicit val authUrlFormat : Format[AuthUrl] = Json.format[AuthUrl]
  implicit val requestSessionFormat : Format[RequestSession] = Json.format[RequestSession]
  implicit val resumeSessionFormat : Format[ResumeSession] = Json.format[ResumeSession]
  implicit val needsAuthorizationFormat : Format[NeedsAuthorization] = Json.format[NeedsAuthorization]
  implicit val sessionAuthorizedFormat : Format[SessionAuthorized] = Json.format[SessionAuthorized]
  implicit val usernameAndPasswordFormate : Format[UsernameAndPassword] = Json.format[UsernameAndPassword]
  implicit val auth0MigrateFormat : Format[Auth0MigrateNeeded] = Json.format[Auth0MigrateNeeded]
}

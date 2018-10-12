package email

import akka.http.scaladsl.model.Uri.Query
import configuration.LocalAuthConfig
import scalatags.Text.all._
import utils.SecureIdentifier

case class PasswordRecoveryEmail(email: String, recoveryId: SecureIdentifier, localAuthConfig: LocalAuthConfig)
    extends EmailMessage {
  override def asHtml: String = {
    html(
      body(
        p(s"Hello,"),
        p("A password recovery has been requested for your Mathbot account associated with this email address."),
        p(
          "If you requested password recovery, please click this ",
          a(href := localAuthConfig.recoveryEmailUrl.withQuery(Query(("recoveryId", recoveryId.toString))).toString(),
            "link"),
          "."
        ),
        p("If you did not request this password recovery, feel free to ignore this request as only someone with this special link can change your email address."),
        p("Thanks!"),
        p("The Math_Bot Team")
      )
    ).render
  }

  override def from: String = "no-reply@mathbot.com"

  override def to : String = email

  override def subject : String = "MATH_BOT Password Recovery"
}

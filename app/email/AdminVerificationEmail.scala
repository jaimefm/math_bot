package email

import akka.http.scaladsl.model.Uri.Query
import configuration.AdminConfig
import scalatags.Text.all._
import utils.SecureIdentifier

case class AdminVerificationEmail(adminReqEmail: String, authenticationId: SecureIdentifier, adminConfig: AdminConfig)
    extends EmailMessage {
  override def asHtml: String = {
    html(
      body(
        p("Hello Admin,"),
        p("You have been designated by Mathbot.com to approve or disapprove admin users"),
        p(
          s"$adminReqEmail is requesting admin access to Mathbot. This will give this user permission to access Mathbot's admin console."
        ),
        p(
          "If you approve ",
          a(href := adminConfig.approvedUrl
              .withQuery(Query(("authenticationId", authenticationId.toString)))
              .toString(),
            "APPROVE"),
          "."
        ),
        p(
          "If you reject ",
          a(href := adminConfig.rejectedUrl
              .withQuery(Query(("authenticationId", authenticationId.toString)))
              .toString(),
            "REJECT"),
          "."
        ),
        p("Thanks!")
      )
    ).render
  }
  override def from: String = "no-reply@mathbot.com"
  override def to: String = adminConfig.custodianEmail
  override def subject: String = "MATH_BOT Admin Access Request"
}

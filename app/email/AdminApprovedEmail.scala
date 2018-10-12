package email
import scalatags.Text.all._

case class AdminApprovedEmail(email: String) extends EmailMessage {
  override def asHtml: String = {
    html(
      body(
        p("Hello,"),
        p("You have been given admin status for Mathbot.com."),
        p("Visit ", a(href := "https://matbot.com/admin", "mathbot.com/admin"), " to checkout the admin console."),
        p("Thanks!")
      )
    ).render
  }
  override def from: String = "no-reply@mathbot.com"
  override def to: String = email
  override def subject: String = "MATH_BOT Admin Request"
}

package actors

import actors.messages._
import akka.actor.Actor
import com.google.inject.Inject
import com.sendgrid._
import email.{EmailMessage, SendGridConfig}
import play.api.libs.json._

class SendgridActor @Inject()(config: SendGridConfig) extends Actor {

  private implicit val brr: OFormat[BounceRecords] = Json.format[BounceRecords]

  override def receive: Receive = {
    case _: EmailBounceCheck =>
      val sg = new SendGrid(config.secretKey)
      val request = new Request()
      request.setMethod(Method.GET)
      request.setEndpoint("suppression/bounces")
      val response = sg.api(request)
      for {
        records <- Json.parse(response.getBody).validate[List[BounceRecords]].asOpt
      } yield {
        sender ! EmailBounceNotification(records)
      }

    case sendMessage: EmailMessage =>
      val fr = new com.sendgrid.Email(sendMessage.from)
      val subject = sendMessage.subject
      val content = new Content("text/html", sendMessage.asHtml)
      val sg = new SendGrid(config.secretKey)
      val mail = new Mail(fr, subject, new com.sendgrid.Email(sendMessage.to), content)
      mail.setTrackingSettings(
        {
          val t = new TrackingSettings
          t.setClickTrackingSetting({
            val s = new ClickTrackingSetting
            s.setEnable(false)
            s
          })
          t
        }
      )
      val request = new Request()
      request.setMethod(Method.POST)
      request.setEndpoint("mail/send")
      request.setBody(mail.build())
      val response = sg.api(request)
      response.getStatusCode match {
        case x if 200 until 299 contains x => sender() ! MailSent(sendMessage)
        case x => sender ! MailFailed(s"StatusCode: $x", sendMessage)
      }

    case deleteBounceMessage: DeleteBounce =>
      val sg = new SendGrid(config.secretKey)
      val request = new Request()
      request.setMethod(Method.DELETE)
      request.setEndpoint(s"suppression/bounces/${deleteBounceMessage.email}")
      //request.addQueryParam("email_address", deleteBounceMessage.email)
      sg.api(request)
    // Log when response fails
  }
}

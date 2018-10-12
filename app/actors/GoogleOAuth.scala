package actors

import actors.GoogleApiHelpers.GoogleTokens
import actors.messages.auth.{ GoogleTokensFromCodeSuccess, RequestTokensFromCode, TokensFromCodeFailure }
import akka.actor.Actor
import akka.http.scaladsl.model._
import akka.http.scaladsl.util.FastFuture
import akka.http.scaladsl.{ Http, HttpExt }
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.google.inject.{ Inject, Singleton }
import configuration.GoogleApiConfig
import loggers.{ AkkaSemanticLog, SemanticLog }
import play.api.libs.json.JsObject
import utils.{ AkkaToPlayMarshaller, JwtTokenParser }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Failure

@Singleton
class GoogleOAuth @Inject()(
                 val jwtTokenParser: JwtTokenParser,
                 val config : GoogleApiConfig
) extends Actor  {

  import HttpMethods._

  implicit val executionContext : ExecutionContext = context.dispatcher
  implicit val materializer : akka.stream.Materializer = ActorMaterializer()

  val http : HttpExt = Http(context.system)

  val log = new AkkaSemanticLog(context.system, this)

  override def aroundReceive(receive : Receive, msg : Any) : Unit = {
    log.info(Seq(SemanticLog.tags.description(msg.toString)))
    super.aroundReceive(receive, msg)
  }

  def retrieveTokens(code : String) : Future[Either[GoogleTokens, String]] = {
    for {
      response <- http.singleRequest(
        HttpRequest(
          method = POST,
          uri = config.authTokenUrl,
          entity = FormData(
            "code" -> code,
            "client_id" -> config.clientId,
            "client_secret" -> config.clientSecret,
            "redirect_uri" -> config.authRedirectUrl.toString(),
            "grant_type" -> "authorization_code"
          ).toEntity
        )
      )
      tokensOrError <- AkkaToPlayMarshaller.unmarshalToPlayJson(response)
      validatedOrError = tokensOrError match {
        case Left(Some(t)) =>
          val tokens = t.asInstanceOf[JsObject]
          jwtTokenParser.parseAndVerify(tokens("id_token").as[String]) match {
            case Some(idToken) =>   Left(GoogleTokens(
              access_token = tokens("access_token").as[String],
              expires_in = tokens("expires_in").as[Long],
              token_type = tokens("token_type").as[String],
              refresh_token = tokens.value.get("refresh_token").map(_.as[String]),
              id_token = idToken
            ))
            case _ =>
              Right("Unable to verify jwt")
          }
        case Left(None) =>
          Right("Unable to verity jwt")
        case Right(reason) =>
          Right(s"Status: ${reason._1} reason: ${reason._2}")
      }
    } yield validatedOrError

  }

  override def receive : Receive = {
    case RequestTokensFromCode(sessionId, code) =>
      retrieveTokens(code) map {
        case Left(tokens) =>
          GoogleTokensFromCodeSuccess(sessionId, tokens)
        case Right(reason) =>
          TokensFromCodeFailure(sessionId, config.oauthUrl, reason)
      } pipeTo sender() onComplete {
        // In practice this is usually a connectivity error. Revisit in production to see if there is a triage.
        case Failure(e) => sender() ! TokensFromCodeFailure(sessionId, config.oauthUrl, e.toString)
        case _ => // Success handled by the pipeTo
      }
  }
}

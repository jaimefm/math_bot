package actors

import actors.messages.auth.{GithubTokensFromCodeSuccess, RequestTokensFromCode, TokensFromCodeFailure}
import akka.actor.Actor
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{headers, _}
import akka.http.scaladsl.util.FastFuture
import akka.http.scaladsl.{Http, HttpExt}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.google.inject.{Inject, Singleton}
import configuration.GithubApiConfig
import loggers.{AkkaSemanticLog, SemanticLog}
import models._
import utils.{AkkaToPlayMarshaller, SecureIdentifier}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@Singleton
class GithubOAuth @Inject()(
    val config: GithubApiConfig
) extends Actor {

  import HttpMethods._
  import models.GithubAccessToken._
  import models.GithubEmail._
  import models.GithubUser._

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val materializer: akka.stream.Materializer = ActorMaterializer()

  val http: HttpExt = Http(context.system)

  val log = new AkkaSemanticLog(context.system, this)

  override def aroundReceive(receive: Receive, msg: Any): Unit = {
    log.info(Seq(SemanticLog.tags.description(msg.toString)))
    super.aroundReceive(receive, msg)
  }

  def retrieveTokens(sessionId: SecureIdentifier, code: String): Future[Either[GithubAccessToken, String]] = {
    for {
      response <- http.singleRequest(
        HttpRequest(
          method = POST,
          uri = config.authTokenUrl,
          headers = List(headers.Accept(MediaRange(MediaTypes.`application/json`))),
          entity = FormData(
            "code" -> code,
            "client_id" -> config.clientId,
            "client_secret" -> config.clientSecret,
            "redirect_uri" -> config.authRedirectUrl.toString(),
            "state" -> sessionId.toString
          ).toEntity
        )
      )
      tokensOrError <- AkkaToPlayMarshaller.unmarshalToPlayJson(response)
    } yield {
      tokensOrError match {
        case Left(Some(tokens)) =>
          Left(tokens.as[GithubAccessToken])
        case Left(None) => Right("Unable to verity jwt")
        case Right(reason) => Right(s"Status: ${reason._1} reason: ${reason._2}")
      }
    }

  }

  def retrievePublicEmails(tokens: GithubAccessToken): Future[Either[Seq[GithubEmail], String]] = {
    for {
      response <- http.singleRequest(
        HttpRequest(
          method = GET,
          uri = config.publicEmailsUrl
            .withQuery(
              Query(
                "access_token" -> tokens.access_token
              )
            ),
          headers = List(headers.Accept(MediaRange(MediaTypes.`application/json`)))
        )
      )
      emailsOrError <- AkkaToPlayMarshaller.unmarshalToPlayJson(response)
    } yield {
      emailsOrError match {
        case Left(Some(emails)) =>
          Left(emails.as[Seq[GithubEmail]])
        case Left(None) => Right("Unable to parse email address")
        case Right(reason) => Right(s"Status: ${reason._1} reason: ${reason._2}")
      }
    }
  }

  def retrieveUser(tokens: GithubAccessToken): Future[Either[GithubUser, String]] = {
    for {
      response <- http.singleRequest(
        HttpRequest(
          method = GET,
          uri = config.userUrl
            .withQuery(
              Query(
                "access_token" -> tokens.access_token
              )
            ),
          headers = List(headers.Accept(MediaRange(MediaTypes.`application/json`)))
        )
      )
      userOrError <- AkkaToPlayMarshaller.unmarshalToPlayJson(response)
    } yield {
      userOrError match {
        case Left(Some(user)) =>
          Left(user.as[GithubUser])
        case Left(None) => Right("Unable to parse user")
        case Right(reason) => Right(s"Status: ${reason._1} reason: ${reason._2}")
      }
    }
  }

  override def receive: Receive = {
    case RequestTokensFromCode(sessionId, code) =>
      (for {
        tokenEither <- retrieveTokens(sessionId, code)
        userEither <- tokenEither match {
          case Left(tokens) =>
            retrieveUser(tokens)
          case _ =>
            FastFuture.successful(Right("Not authorized to lookup user"))
        }
        emailsEither <- tokenEither match {
          case Left(tokens) =>
            retrievePublicEmails(tokens)
          case _ =>
            FastFuture.successful(Right("Not authorized to lookup email"))
        }
      } yield {

        (tokenEither, userEither, emailsEither) match {
          case (Left(tokens), Left(user), Left(emails)) =>
            val jwt = JwtToken(
              iss = config.issuer,
              sub = user.id.toString,
              email = emails.find(_.primary).map(_.email).orElse(user.email).getOrElse(""),
              name = user.name,
              picture = Some(user.avatar_url)
            )
            GithubTokensFromCodeSuccess(sessionId,
                                        GithubTokens(
                                          access_token = tokens.access_token,
                                          scope = tokens.scope.split(':'),
                                          token_type = tokens.token_type,
                                          id_token = jwt
                                        ))
          case (Right(reason), _, _) =>
            TokensFromCodeFailure(sessionId, config.oauthUrl, reason)
          case (_, Right(reason), _) =>
            TokensFromCodeFailure(sessionId, config.userUrl, reason)
          case (_, _, Right(reason)) =>
            TokensFromCodeFailure(sessionId, config.userUrl, reason)
        }
      }) pipeTo sender() onComplete {
        // In practice this is usually a connectivity error. Revisit in production to see if there is a triage.
        case Failure(e) => sender() ! TokensFromCodeFailure(sessionId, config.oauthUrl, e.toString)
        case _ => // Success handled by the pipeTo
      }
  }
}

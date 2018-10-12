package actors
import actors.messages.{Auth0Authenticate, Auth0Authorized}
import akka.actor.Actor
import akka.http.scaladsl.{Http, HttpExt}
import com.google.inject.{Inject, Singleton}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import configuration.Auth0Config
import loggers.AkkaSemanticLog
import play.api.libs.json.Json
import akka.pattern.pipe
import utils.{AkkaToPlayMarshaller, JwtTokenParser}

import scala.concurrent.ExecutionContext

@Singleton
class Auth0Actor @Inject()(
    val config: Auth0Config
) extends Actor {
  import HttpMethods._

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val materializer: akka.stream.Materializer = ActorMaterializer()

  val http: HttpExt = Http(context.system)
  val log = new AkkaSemanticLog(context.system, this)

  override def receive: Receive = {
    case Auth0Authenticate(username, password) =>
      (for {
        response <- http.singleRequest(
          HttpRequest(
            method = POST,
            uri = config.url,
            headers = List(headers.Accept(MediaRange(MediaTypes.`application/json`))),
            entity = FormData(
              "grant_type" -> config.grantType,
              "realm" -> config.realm,
              "username" -> username,
              "password" -> password,
              "audience" -> config.audience,
              "client_id" -> config.clientId,
              "client_secret" -> config.clientSecret
            ).toEntity
          )
        )
        responseOrError <- AkkaToPlayMarshaller.unmarshalToPlayJson(response)
      } yield {
        responseOrError match {
          case Left(Some(auth0Creds)) =>
            Left(auth0Creds.as[Auth0Authorized])
          case Left(None) => Right("Incorrect password or username")
          case Right(reason) => Right("Incorrect password or username")
        }
      }) pipeTo sender()
  }
}

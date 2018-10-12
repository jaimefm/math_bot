package controllers

import actors.ActorTags
import actors.messages.auth._
import actors.messages.playeraccount.CreateAccount
import actors.messages.{Auth0Authenticate, Auth0Authorized}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import com.digitaltangible.playguard._
import com.google.inject.name.Named
import configuration.{ActorConfig, GithubApiConfig, GoogleApiConfig, LocalAuthConfig}
import daos._
import email._
import javax.inject.Inject
import loggers.SemanticLog
import models._
import org.bouncycastle.crypto.generators.SCrypt
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import utils.{JwtTokenParser, SecureIdentifier}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object AuthController {
  //noinspection FoldTrueAnd
  def compareWithConstantTime(a: Array[Byte], b: Array[Byte]): Boolean = {
    // Do not EVER simplify this code to a forall because security requires all bytes
    // to be compared to avoid timing attacks.  Intellij will desperately try to simplify
    // the code but avoid writing anything that can exit early if a byte doesn't match. The
    // XOR in the middle is just a way to keep the JVM from optimizing the code, which if
    // it was a boolean compare it just might do something clever.
    a.zip(b).map(p => p._1 ^ p._2).sum[Int] == 0
  }

  def hashCredential(credential: UsernameAndPassword,
                     salt: SecureIdentifier,
                     iteration: Int,
                     blocksize: Int,
                     hashByteSize: Int): Array[Byte] =
    SCrypt.generate(credential.password.getBytes, salt.toByteArray, iteration, blocksize, 1, hashByteSize)
}

class AuthController @Inject()(
    val sessionDAO: SessionDAO,
    val sessionCache: SessionCache,
    val localCredential: LocalCredentialDao,
    val playerTokens: PlayerTokenDAO,
    val auth0legacy: Auth0LegacyDao,
    @Named(ActorTags.googleOAuth) val googleOauth: ActorRef,
    @Named(ActorTags.githubOAuth) val githubOAuth: ActorRef,
    @Named(ActorTags.sendGrid) val sendGrid: ActorRef,
    @Named(ActorTags.auth0) val auth0: ActorRef,
    @Named(ActorTags.playerAccount) val playerAccount: ActorRef,
    val jwtParser: JwtTokenParser,
    val googleConfig: GoogleApiConfig,
    val githubConfig: GithubApiConfig,
    val actorConfig: ActorConfig,
    val mathbotConfig: LocalAuthConfig,
    val logger: SemanticLog,
    implicit val system: ActorSystem,
    implicit val conf: play.api.Configuration
)(implicit ec: ExecutionContext)
    extends Controller {
  import AuthController._

  // Allow 10 subsequent requests, the renew token every 5 seconds
  private val ipRateLimitAction = IpRateLimitAction(new RateLimiter(10, 1f / 5, "test limit by IP address")) {
    implicit r: RequestHeader =>
      TooManyRequests(s"""rate limit for ${r.remoteAddress} exceeded""")
  }

  private def generateNeedsAuthorization(sessionId: SecureIdentifier) = {
    NeedsAuthorization(
      sessionId,
      Seq(
        AuthUrl(
          "google",
          googleConfig.oauthUrl
            .withQuery(
              Query(
                "client_id" -> googleConfig.clientId,
                "scope" -> googleConfig.scopes.mkString(" "),
                "redirect_uri" -> googleConfig.authRedirectUrl.toString(),
                "state" -> sessionId.toString,
                "response_type" -> "code"
              )
            )
            .toString
        ),
        AuthUrl(
          "github",
          githubConfig.oauthUrl
            .withQuery(
              Query(
                "client_id" -> githubConfig.clientId,
                "scope" -> githubConfig.scopes.mkString(":"),
                "redirect_uri" -> githubConfig.authRedirectUrl.toString(),
                "state" -> sessionId.toString
              )
            )
            .toString
        ),
        AuthUrl(
          "mathbotSignup",
          mathbotConfig.signupUrl
            .withQuery(
              Query(
                "state" -> sessionId.toString
              )
            )
            .toString()
        ),
        AuthUrl(
          "mathbotAuth",
          mathbotConfig.authUrl
            .withQuery(
              Query(
                "state" -> sessionId.toString
              )
            )
            .toString()
        )
      )
    )
  }

  private def generateSessionAuthorized(sessionId: SecureIdentifier, idToken: JwtToken) =
    SessionAuthorized(sessionId,
                      idToken.name,
                      idToken.picture.getOrElse(""),
                      s"${idToken.getIssuerShortName}|${idToken.sub}",
                      idToken.email)

  private implicit val timeout: Timeout = actorConfig.timeout

  private val scryptIteration = Math.pow(2, mathbotConfig.scryptIterationExponent.toDouble).toInt

  import actors.messages.auth.AuthFormatters._

  def passwordRecovery(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.flatMap(_.asOpt[PasswordRecovery]) match {
      case Some(PasswordRecovery(email)) =>
        localCredential.find(email) flatMap {
          case Some(credential) =>
            val recoveryId = SecureIdentifier(mathbotConfig.recoveryIdByteWidth)
            localCredential.insertOrUpdate(credential.accountId, credential.copy(recoveryId = Some(recoveryId))) map {
              _ =>
                sendGrid ! PasswordRecoveryEmail(email, recoveryId, mathbotConfig)
                Ok("Check your email for the reset link, be sure to check your spam folder to.")
            }
          case None =>
            FastFuture.successful(NotFound("Email not found in system"))
        }
      case None =>
        FastFuture.successful(BadRequest("Invalid body"))
    }
  }

  def updatePassword(): Action[AnyContent] = Action.async { implicit request =>
    request.getQueryString("recoveryId").map(SecureIdentifier(_)) match {
      case Some(recoveryId) =>
        request.body.asJson.flatMap(_.asOpt[PasswordUpdate]) match {
          case Some(PasswordUpdate(email, password)) =>
            localCredential.find(email) flatMap {
              case Some(credential) if credential.recoveryId.contains(recoveryId) =>
                val sessionId = SecureIdentifier.apply(mathbotConfig.sessionIdByteWidth)
                storeCredential(sessionId,
                                credential.accountId,
                                SignUpForm(credential.username, credential.name, credential.picture, password)) map {
                  jwt =>
                    val sessionAuthorized = generateSessionAuthorized(sessionId, jwt)
                    Ok(Json.toJson(sessionAuthorized)).withCookies(Cookie("player-session", sessionId.toString))
                }
              case _ =>
                FastFuture.successful(Unauthorized("Password update rejected"))
            }
          case _ =>
            FastFuture.successful(BadRequest("Invalid body"))
        }
      case _ => FastFuture.successful(BadRequest("Invalid query string"))
    }
  }

  def usernameExists(): Action[AnyContent] = (Action andThen ipRateLimitAction).async { implicit request =>
    request.body.asJson.flatMap(_.asOpt[ExistsRequest]) match {
      case Some(ExistsRequest(username)) =>
        for {
          maybeUser <- localCredential.find(username)
        } yield
          Ok(maybeUser match {
            case Some(_) => Json.obj("exists" -> true)
            case None => Json.obj("exists" -> false)
          })
      case None => FastFuture.successful(BadRequest("Invalid body"))
    }
  }

  def requestSession(): Action[AnyContent] = Action.async { implicit request =>
    val sid = SecureIdentifier(mathbotConfig.sessionIdByteWidth)
    sessionCache.put(sid, None)

    Future { Ok(Json.toJson(generateNeedsAuthorization(sid))) }
  }

  def resumeSession(): Action[AnyContent] = Action.async { implicit request =>
    request.cookies.get("player-session").map(c => ResumeSession(SecureIdentifier(c.value))) match {
      case Some(ResumeSession(sessionId, _)) =>
        for {
          token <- sessionDAO.find(sessionId)
        } yield
          token match {
            case Some(idToken) =>
              val sessionAuthorized = generateSessionAuthorized(sessionId, idToken)
              Ok(Json.toJson(sessionAuthorized)).withCookies(Cookie("player-session", sessionId.toString))
            case _ => Ok(Json.toJson(generateNeedsAuthorization(sessionId)))
          }
      case _ =>
        Future(BadRequest(JsString("No session cookie")))
    }
  }

  def authorizeGoogle(): Action[AnyContent] = Action.async { implicit request =>
    (for {
      code <- request.getQueryString("code")
      state <- request.getQueryString("state")
    } yield {
      val sessionId = SecureIdentifier(state)
      for {
        sessionValid <- FastFuture {
          Try {
            sessionCache.get(sessionId).isDefined
          }
        }
        tokenResult <- sessionValid match {
          case true => googleOauth ? RequestTokensFromCode(sessionId, code)
          case false => FastFuture.successful(SessionNotAuthorized(sessionId, "Invalid sessionId in state/guard code"))
        }
        storedResult <- tokenResult match {
          case GoogleTokensFromCodeSuccess(aSessionId, tokens) =>
            for {
              _ <- sessionDAO.insertOrUpdate(aSessionId, tokens.id_token)
              _ <- playerAccount ? CreateAccount(tokens.id_token)
            } yield Left[JwtToken, String](tokens.id_token)

          case TokensFromCodeFailure(_, _, reason) =>
            FastFuture.successful(
              Right[JwtToken, String](s"Could not verify authorization because of '$reason'")
            )
          case SessionNotAuthorized(_, reason, _) =>
            FastFuture.successful {
              Right[JwtToken, String](s"Unable to authorize requestSession $sessionId because of $reason")
            }
          case msg: Any =>
            logger.error(SemanticLog.tags.message(msg))
            FastFuture.successful(
              Right[JwtToken, String](s"Unexpected message from google oauth actor")
            )
        }
      } yield
        storedResult match {
          case Left(idToken) =>
            val sessionAuthorized = generateSessionAuthorized(sessionId, idToken)
            Ok(Json.toJson(sessionAuthorized)).withCookies(Cookie("player-session", sessionId.toString))
          case Right(reason) => Unauthorized(JsString(reason))
        }
    }).getOrElse(FastFuture.successful(BadRequest("One or more query parameters are missing")))
  }

  def authorizeGithub(): Action[AnyContent] = Action.async { implicit request =>
    (for {
      code <- request.getQueryString("code")
      state <- request.getQueryString("state")
    } yield {
      val sessionId = SecureIdentifier(state)
      for {
        sessionValid <- FastFuture {
          Try {
            sessionCache.get(sessionId).isDefined
          }
        }
        tokenResult <- sessionValid match {
          case true => githubOAuth ? RequestTokensFromCode(sessionId, code)
          case false => FastFuture.successful(SessionNotAuthorized(sessionId, "Invalid sessionId in state/guard code"))
        }
        storedResult <- tokenResult match {
          case GithubTokensFromCodeSuccess(_, tokens) =>
            for {
              _ <- sessionDAO.insertOrUpdate(sessionId, tokens.id_token)
              _ <- playerAccount ? CreateAccount(tokens.id_token)
            } yield Left[JwtToken, String](tokens.id_token)

          case TokensFromCodeFailure(_, _, reason) =>
            FastFuture.successful(
              Right[JwtToken, String](s"Could not verify authorization because of '$reason'")
            )
          case SessionNotAuthorized(_, reason, _) =>
            FastFuture.successful {
              Right[JwtToken, String](s"Unable to authorize requestSession $sessionId because of $reason")
            }
          case msg: Any =>
            logger.error(SemanticLog.tags.message(msg))
            FastFuture.successful(
              Right[JwtToken, String](s"Unexpected message from github oauth actor")
            )
        }
      } yield
        storedResult match {
          case Left(idToken) =>
            val sessionAuthorized = generateSessionAuthorized(sessionId, idToken)
            Ok(Json.toJson(sessionAuthorized)).withCookies(Cookie("player-session", sessionId.toString))
          case Right(reason) => Unauthorized(JsString(reason.toString))
        }
    }).getOrElse(FastFuture.successful(BadRequest("One or more query parameters are missing")))
  }

  private def hashCredential(credential: UsernameAndPassword,
                             salt: SecureIdentifier,
                             iteration: Int,
                             blocksize: Int,
                             hashByteSize: Int): Array[Byte] =
    SCrypt.generate(credential.password.getBytes, salt.toByteArray, iteration, blocksize, 1, hashByteSize)

  private def storeCredential(sessionId: SecureIdentifier, accountId: SecureIdentifier, credential: SignUpForm) = {
    val salt = SecureIdentifier(mathbotConfig.saltByteWidth)
    val hash = hashCredential(
      UsernameAndPassword(username = credential.username, password = credential.password),
      salt,
      scryptIteration,
      mathbotConfig.scryptBlockSize,
      mathbotConfig.hashByteSize
    )
    val lc = LocalCredential(
      accountId,
      None,
      credential.username,
      credential.name,
      credential.picture,
      salt,
      hash,
      scryptIteration,
      mathbotConfig.scryptBlockSize,
      mathbotConfig.hashByteSize
    )
    localCredential.insertOrUpdate(accountId, lc) map { _ =>
      val jwt = JwtToken(
        iss = "https://mathbot.com",
        sub = lc.accountId.toString,
        email = lc.username,
        name = lc.name,
        picture = lc.picture
      )
      sessionCache.put(sessionId, Some(jwt)).get
    }
  }

  def signupMathbot(): Action[AnyContent] = Action.async { implicit request =>
    val signupFormOpt = for {
      json <- request.body.asJson
      signup <- json.validate[SignUpForm].asOpt
    } yield signup
    signupFormOpt match {
      case Some(signUpForm) =>
        localCredential.find(signUpForm.username) flatMap {
          case Some(_) =>
            FastFuture.successful(Unauthorized("Username already exists"))
          case None =>
            val sessionId = SecureIdentifier.apply(mathbotConfig.sessionIdByteWidth)
            val accountId = SecureIdentifier.apply(mathbotConfig.accountIdByteWidth)
            for {
              jwt <- storeCredential(sessionId, accountId, signUpForm)
              _ <- sessionDAO.insertOrUpdate(sessionId, jwt)
              _ <- playerAccount ? CreateAccount(jwt)
            } yield {
              val sessionAuthorized = generateSessionAuthorized(sessionId, jwt)
              Ok(Json.toJson(sessionAuthorized)).withCookies(Cookie("player-session", sessionId.toString))
            }
        }
      case None =>
        FastFuture.successful(BadRequest("Malformed Json"))
    }
  }

  def authMathbot(): Action[AnyContent] = Action.async { implicit request =>
    val credentialOpt = for {
      json <- request.body.asJson
      signUp <- json.validate[UsernameAndPassword].asOpt
    } yield signUp

    credentialOpt match {
      case Some(credential) =>
        localCredential.find(credential.username) flatMap {
          case None =>
            auth0legacy.find(credential.username).flatMap {
              case Some(legacy) if !legacy.migrated.getOrElse(false) =>
                (auth0 ? Auth0Authenticate(credential.username, credential.password))
                  .mapTo[Either[Auth0Authorized, String]]
                  .flatMap {
                    case Left(auth0Authorized) =>
                      jwtParser.parse(auth0Authorized.id_token) map { jwt =>
                        (for {
                          playerTokenOpt <- playerTokens.getToken(jwt.sub)
                          credentialOpt <- localCredential.find(jwt.email)
                        } yield (playerTokenOpt, credentialOpt)) flatMap {
                          case (_, Some(_)) =>
                            FastFuture.successful(Unauthorized("User already migrated"))
                          case (None, _) =>
                            FastFuture.successful(Unauthorized("Player data missing for credentials"))
                          case (Some(playerToken), _) =>
                            val sessionId = SecureIdentifier(mathbotConfig.sessionIdByteWidth)
                            val accountId = SecureIdentifier(mathbotConfig.accountIdByteWidth)
                            for {
                              migratedJwt <- storeCredential(
                                sessionId,
                                accountId,
                                SignUpForm(jwt.email, jwt.name, jwt.picture, credential.password)
                              )
                              _ <- auth0legacy.markMigrated(jwt.email)
                              migratedTokenId = s"${migratedJwt.getIssuerShortName}|${migratedJwt.sub}"
                              _ <- playerTokens.insert(playerToken.copy(token_id = migratedTokenId))
                              _ <- playerTokens.delete(jwt.sub)
                              _ <- sessionDAO.insertOrUpdate(sessionId, jwt)
                              _ <- playerAccount ? CreateAccount(jwt)
                            } yield {
                              val sessionAuthorized = generateSessionAuthorized(sessionId, jwt)
                              Ok(Json.toJson(sessionAuthorized))
                                .withCookies(Cookie("player-session", sessionId.toString))
                            }
                        }
                      } match {
                        case Some(future) =>
                          future
                        case None =>
                          FastFuture.successful(Unauthorized("Invalid json web token"))
                      }
                    case Right(errMsg) =>
                      FastFuture.successful(Unauthorized(errMsg))
                  }
              case None =>
                FastFuture.successful(Unauthorized("Username does not exist"))
            }

          case Some(lc) =>
            FastFuture {
              Try {
                val hash = hashCredential(credential, lc.salt, lc.iterations, lc.blockSize, lc.hashSize)

                compareWithConstantTime(lc.hash, hash) match {
                  case true =>
                    val jwt = JwtToken(
                      iss = "https://mathbot.com",
                      sub = lc.accountId.toString,
                      email = lc.username,
                      name = lc.name,
                      picture = lc.picture
                    )
                    val sessionId = SecureIdentifier(mathbotConfig.sessionIdByteWidth)
                    sessionCache.put(sessionId, Some(jwt))
                    val sessionAuthorized = generateSessionAuthorized(sessionId, jwt)
                    sessionDAO.insertOrUpdate(sessionId, jwt)
                    playerAccount ! CreateAccount(jwt)
                    Ok(Json.toJson(sessionAuthorized)).withCookies(Cookie("player-session", sessionId.toString))
                  case false =>
                    Unauthorized("Password did not match")
                }
              }
            }
        }
      case None =>
        FastFuture.successful(BadRequest("Malformed Json"))
    }
  }

  def logout(): Action[AnyContent] = Action.async { implicit request =>
    request.cookies.get("player-session").map(c => SecureIdentifier(c.value)) match {
      case Some(secureIdentifier) =>
        sessionDAO.delete(secureIdentifier).map { _ =>
          Ok("Logged out").discardingCookies(DiscardingCookie("player-session"))
        }
      case None => FastFuture.successful(Ok("Logged out"))
    }
  }
}

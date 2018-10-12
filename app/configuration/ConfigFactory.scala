package configuration

import akka.http.scaladsl.model.Uri
import akka.util.Timeout
import com.google.inject.Inject
import email.SendGridConfig

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

object ConfigFactory {
  object mathbot {
    final val maxProgramSteps = "mathbot.maxProgramSteps"
    object oauth {
      object google {
        final val scopes: String = "mathbot.oauth.google.scopes"
        final val clientSecret: String = "mathbot.oauth.google.clientSecret"
        final val clientId: String = "mathbot.oauth.google.clientId"
        final val redirectUrl: String = "mathbot.oauth.google.redirectUrl"
        final val authUrl: String = "mathbot.oauth.google.authUrl"
        final val tokenUrl: String = "mathbot.oauth.google.tokenUrl"
        // Location of the PEM format certificates used to verify JWT tokens
        final val pemUrl: String = "mathbot.oauth.google.pemUrl"
      }
      object github {
        final val scopes: String = "mathbot.oauth.github.scopes"
        final val clientSecret: String = "mathbot.oauth.github.clientSecret"
        final val clientId: String = "mathbot.oauth.github.clientId"
        final val redirectUrl: String = "mathbot.oauth.github.redirectUrl"
        final val authUrl: String = "mathbot.oauth.github.authUrl"
        final val tokenUrl: String = "mathbot.oauth.github.tokenUrl"
        final val publicEmails: String = "mathbot.oauth.github.publicEmails"
        final val issuer: String = "mathbot.oauth.github.issuer"
        final val user: String = "mathbot.oauth.github.user"
      }
      object auth0 {
        final val pemUrl: String = "mathbot.oauth.auth0.pemUrl"
      }
    }
    object actors {
      final val timeout: String = "mathbot.actors.timeout"
    }
    object mongodb {
      val name: String = "mathbot.mongodb.name"
      val url: String = "mathbot.mongodb.url"
    }
    object localauth {
      val signupUrl: String = "mathbot.localauth.signupUrl"
      val authUrl: String = "mathbot.localauth.authUrl"
      val recoveryEmailUrl: String = "mathbot.localauth.recoveryEmailUrl"
      val accountIdByteWidth: String = "mathbot.localauth.accountIdByteWidth"
      val saltByteWidth: String = "mathbot.localauth.saltByteWidth"
      val sessionIdByteWidth: String = "mathbot.localauth.sessionIdByteWidth"
      val scryptIterationExponent: String = "mathbot.localauth.scryptIterationExponent"
      val scryptBlockSize: String = "mathbot.localauth.sessionIdByteWidth"
      val hashByteSize: String = "mathbot.localauth.hashByteSize"
      val recoveryIdByteWidth: String = "mathbot.localauth.recoveryIdByteWidth"
    }
    object auth0 {
      val pemUrl: String = "mathbot.auth0.pemUrl"
      val url: String = "mathbot.auth0.url"
      val grantType: String = "mathbot.auth0.grantType"
      val realm: String = "mathbot.auth0.realm"
      val audience: String = "mathbot.auth0.audience"
      val clientId: String = "mathbot.auth0.clientId"
      val clientSecret: String = "mathbot.auth0.clientSecret"
    }
    object sendgrid {
      val secretKey: String = "mathbot.sendgrid.secretKey"
    }
    object admin {
      val authIdByteWidth: String = "mathbot.admin.authIdByteWidth"
      val approvedUrl: String = "mathbot.admin.approvedUrl"
      val rejectedUrl: String = "mathbot.admin.rejectedUrl"
      val custodianEmail: String = "mathbot.admin.custodianEmail"
    }
  }
}

class ConfigFactory @Inject()(playConfig: play.api.Configuration) {
  import ConfigFactory._

  private def exWrap[C](path: String, readers: (String => Option[C])*) = {
    readers.map(r => r(path)).find(_.isDefined).flatten getOrElse {
      throw new MissingConfigurationException(path)
    }
  }

  private def wrap[T](path: String, converter: String => T): T =
    playConfig.getString(path).map(converter(_)) getOrElse {
      throw new MissingConfigurationException(path)
    }

  private def envGet(path: String) = {
    sys.env.get(path.replace(".", "_"))
  }

  def googleApiConfig(): GoogleApiConfig = {
    GoogleApiConfig(
      oauthUrl = exWrap(mathbot.oauth.google.authUrl, path => playConfig.getString(path).map(Uri(_))),
      authRedirectUrl = exWrap(mathbot.oauth.google.redirectUrl, path => playConfig.getString(path).map(Uri(_))),
      authTokenUrl = exWrap(mathbot.oauth.google.tokenUrl, path => playConfig.getString(path).map(Uri(_))),
      clientId = exWrap(mathbot.oauth.google.clientId, envGet, playConfig.getString(_)),
      clientSecret = exWrap(mathbot.oauth.google.clientSecret, envGet, playConfig.getString(_)),
      scopes = exWrap(mathbot.oauth.google.scopes, playConfig.getStringList(_).map(s => s.asScala)),
      oauthPemUrl = exWrap(mathbot.oauth.google.pemUrl, path => playConfig.getString(path).map(Uri(_)))
    )
  }

  def githubApiConfig(): GithubApiConfig =
    GithubApiConfig(
      oauthUrl = exWrap(mathbot.oauth.github.authUrl, path => playConfig.getString(path).map(Uri(_))),
      authRedirectUrl = exWrap(mathbot.oauth.github.redirectUrl, path => playConfig.getString(path).map(Uri(_))),
      authTokenUrl = exWrap(mathbot.oauth.github.tokenUrl, path => playConfig.getString(path).map(Uri(_))),
      publicEmailsUrl = exWrap(mathbot.oauth.github.publicEmails, path => playConfig.getString(path).map(Uri(_))),
      userUrl = exWrap(mathbot.oauth.github.user, path => playConfig.getString(path).map(Uri(_))),
      clientId = exWrap(mathbot.oauth.github.clientId, envGet, playConfig.getString(_)),
      clientSecret = exWrap(mathbot.oauth.github.clientSecret, envGet, playConfig.getString(_)),
      scopes = exWrap(mathbot.oauth.github.scopes, playConfig.getStringList(_).map(s => s.asScala)),
      issuer = exWrap(mathbot.oauth.github.issuer, path => playConfig.getString(path))
    )

  def adminConfig: AdminConfig =
    AdminConfig(
      authIdByteWidth = wrap(mathbot.admin.authIdByteWidth, _.toInt),
      rejectedUrl = wrap(mathbot.admin.rejectedUrl, Uri(_)),
      approvedUrl = wrap(mathbot.admin.approvedUrl, Uri(_)),
      custodianEmail = exWrap(mathbot.admin.custodianEmail, envGet, playConfig.getString(_))
    )

  def auth0Config: Auth0Config =
    Auth0Config(
      pemUrl = wrap(mathbot.auth0.pemUrl, _.toString),
      url = wrap(mathbot.auth0.url, _.toString),
      grantType = wrap(mathbot.auth0.grantType, _.toString),
      realm = wrap(mathbot.auth0.realm, _.toString),
      audience = wrap(mathbot.auth0.audience, _.toString),
      clientId = exWrap(mathbot.auth0.clientId, envGet, playConfig.getString(_)),
      clientSecret = exWrap(mathbot.auth0.clientSecret, envGet, playConfig.getString(_))
    )

  def actorConfig(): ActorConfig = {
    ActorConfig(
      timeout = exWrap(mathbot.actors.timeout,
                       path => playConfig.getString(path).map(s => Timeout(Duration(s).asInstanceOf[FiniteDuration])))
    )
  }

  def mongoConfig(): MongoConfig = {
    MongoConfig(
      name = exWrap(mathbot.mongodb.name, name => playConfig.getString(name)),
      url = exWrap(mathbot.mongodb.url, url => playConfig.getString(url))
    )
  }

  def localAuthConfig: LocalAuthConfig =
    LocalAuthConfig(
      signupUrl = wrap(mathbot.localauth.signupUrl, Uri(_)),
      authUrl = wrap(mathbot.localauth.authUrl, Uri(_)),
      recoveryEmailUrl = wrap(mathbot.localauth.recoveryEmailUrl, Uri(_)),
      accountIdByteWidth = wrap(mathbot.localauth.accountIdByteWidth, _.toInt),
      saltByteWidth = wrap(mathbot.localauth.saltByteWidth, _.toInt),
      sessionIdByteWidth = wrap(mathbot.localauth.sessionIdByteWidth, _.toInt),
      scryptIterationExponent = wrap(mathbot.localauth.scryptIterationExponent, _.toInt),
      scryptBlockSize = wrap(mathbot.localauth.scryptBlockSize, _.toInt),
      hashByteSize = wrap(mathbot.localauth.hashByteSize, _.toInt),
      recoveryIdByteWidth = wrap(mathbot.localauth.recoveryIdByteWidth, _.toInt)
    )

  def sendGridConfig: SendGridConfig =
    SendGridConfig(
      secretKey = exWrap(mathbot.sendgrid.secretKey, envGet, playConfig.getString(_))
    )
}

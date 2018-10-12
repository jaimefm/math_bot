package modules
import actors._
import akka.actor.ActorSystem
import com.google.inject.{ AbstractModule, Provides }
import configuration._
import email.SendGridConfig
import loggers.{ AkkaSemanticLog, SemanticLog }
import models.{ Auth0LegacyUser, JwtToken, PlayerAccount }
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{ MongoClient, MongoDatabase }
import play.api.libs.concurrent.AkkaGuiceSupport
import utils.SecureIdentifier.SecureIdentifierCodec

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[GoogleOAuth](ActorTags.googleOAuth)
    bindActor[GithubOAuth](ActorTags.githubOAuth)
    bindActor[SendgridActor](ActorTags.sendGrid)
    bindActor[Auth0Actor](ActorTags.auth0)
    bindActor[PlayerAccountActor](ActorTags.playerAccount)
  }

  @Provides
  def provideMongoDatabase(configFactory: ConfigFactory): MongoDatabase = {
    val name = configFactory.mongoConfig().name
    val url = configFactory.mongoConfig().url
    // To directly connect to the default server localhost on port 27017
    val mongoClient: MongoClient = MongoClient(url)
    mongoClient.getDatabase(name)
  }

  @Provides
  def provideGoogleApiConfig(configFactory: ConfigFactory): GoogleApiConfig = {
    configFactory.googleApiConfig()
  }

  @Provides
  def provideGithubApiConfig(configFactory: ConfigFactory): GithubApiConfig = {
    configFactory.githubApiConfig()
  }

  @Provides
  def provideActorConfig(configFactory: ConfigFactory): ActorConfig = {
    configFactory.actorConfig()
  }

  @Provides
  def provideSemanticLog(system: ActorSystem): SemanticLog = new AkkaSemanticLog[String](system, "global")

  @Provides
  def mongoCodecs(secureIdentifierCodec: SecureIdentifierCodec): Seq[Codec[_]] = Seq(secureIdentifierCodec)

  @Provides
  def mongoCodecProviders: Seq[CodecProvider] =
    Seq(Macros.createCodecProvider[JwtToken], Macros.createCodecProvider[Auth0LegacyUser], Macros.createCodecProvider[PlayerAccount])

  @Provides
  def provideLocalAuthConfig(configFactory: ConfigFactory): LocalAuthConfig =
    configFactory.localAuthConfig

  @Provides
  def provideSendGridConfig(configFactory: ConfigFactory): SendGridConfig =
    configFactory.sendGridConfig

  @Provides
  def provideAuth0Config(configFactory: ConfigFactory): Auth0Config =
    configFactory.auth0Config

  @Provides
  def provideAdminConfig(configFactory: ConfigFactory): AdminConfig =
    configFactory.adminConfig
}

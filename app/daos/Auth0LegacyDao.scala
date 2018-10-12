package daos

import com.google.inject.Inject
import loggers.SemanticLog
import models.Auth0LegacyUser
import org.bson.codecs.Codec
import org.bson.codecs.configuration.{ CodecProvider, CodecRegistries }
import org.bson.codecs.configuration.CodecRegistries.{ fromProviders, fromRegistries }
import org.mongodb.scala.{ MongoDatabase, _ }
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.Updates._

import scala.concurrent.{ ExecutionContext, Future }

class Auth0LegacyDao @Inject()(val db : MongoDatabase,
                               aLogger     : SemanticLog,
                               aCodecs     : Seq[Codec[_]] = Seq.empty[Codec[_]],
                               aProviders  : Seq[CodecProvider]
                          )(implicit val ec : ExecutionContext)
{

  private val collectionLabel = 'auth0legacy
  private val emailColumnLabel = 'email
  private val migratedColumnLabel = 'migrated

  private val codecRegistry = fromRegistries(
    fromProviders(aProviders :_*),
    CodecRegistries.fromCodecs(aCodecs :_*),
    DEFAULT_CODEC_REGISTRY
  )

  val collection: MongoCollection[Auth0LegacyUser] =
    db.getCollection[Auth0LegacyUser](collectionLabel.name).withCodecRegistry(codecRegistry)

  def find(email : String) : Future[Option[Auth0LegacyUser]] = collection.find(equal(emailColumnLabel.name, email)).toFuture().map(_.headOption)

  def markMigrated(email : String) : Future[Option[Auth0LegacyUser]] = collection.findOneAndUpdate(equal(emailColumnLabel.name, email), set(migratedColumnLabel.name, true) ).toFutureOption()

  collection.createIndex(ascending(emailColumnLabel.name)).toFuture()
}
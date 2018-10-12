package daos

import com.google.inject.Inject
import com.sendgrid.Email
import loggers.SemanticLog
import models.PlayerAccount
import org.bson.codecs.Codec
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{Completed, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.Updates._
import types.TokenId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PlayerAccountDAO @Inject()(
    db: MongoDatabase,
    aLogger: SemanticLog,
    aCodecs: Seq[Codec[_]] = Seq.empty[Codec[_]],
    aProviders: Seq[CodecProvider]
)(implicit val ec: ExecutionContext) {
  private val collectionLabel = 'playeraccount
  private val tokenIdLabel = 'tokenId
  private val emailLabel = 'email
  private val isAdminLabel = 'isAdmin
  private val lastAccess = 'lastAccess
  private val timesAccessed = 'timesAccessed
  private val maxLevel = 'maxLevel
  private val maxStep = 'maxStep

  private val codecRegistry = fromRegistries(
    fromProviders(aProviders: _*),
    CodecRegistries.fromCodecs(aCodecs: _*),
    DEFAULT_CODEC_REGISTRY
  )

  val collection: MongoCollection[PlayerAccount] =
    db.getCollection[PlayerAccount](collectionLabel.name).withCodecRegistry(codecRegistry)

  def put(pa: PlayerAccount): Future[Completed] = collection.insertOne(pa).toFuture()

  def find(tokenId: TokenId): Future[Option[PlayerAccount]] =
    collection.find(equal(tokenIdLabel.name, tokenId)).headOption()

  def setAdmin(tokenId: Option[String], email: Option[String], isAdmin: Boolean): Future[Option[PlayerAccount]] =
    for {
      result <- collection
        .findOneAndUpdate(tokenId match {
          case Some(id) => equal(tokenIdLabel.name, id)
          case None => equal(emailLabel.name, email.getOrElse(""))
        }, set(isAdminLabel.name, isAdmin))
        .toFutureOption()
    } yield result

  def updateAccess(tokenId: TokenId): Future[Option[PlayerAccount]] =
    for {
      result <- collection
        .findOneAndUpdate(equal(tokenIdLabel.name, tokenId),
                          combine(currentDate(lastAccess.name), inc(timesAccessed.name, 1)))
        .toFutureOption()
    } yield result

  def updateMaxLevelAndStep(tokenId: TokenId, ml: String, ms: String): Future[Option[PlayerAccount]] =
    for {
      result <- collection
        .findOneAndUpdate(equal(tokenIdLabel.name, tokenId), combine(set(maxLevel.name, ml), set(maxStep.name, ms)))
        .toFutureOption()
    } yield result

  collection.createIndex(ascending(tokenIdLabel.name)).toFuture().onComplete {
    case Success(_) =>
      aLogger.debug(SemanticLog.tags.index(collectionLabel, tokenIdLabel, "Created index"))
    case Failure(t) =>
      aLogger.error(SemanticLog.tags.index(collectionLabel, tokenIdLabel, t, "Failed to create index"))
  }
}

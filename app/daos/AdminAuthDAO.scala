package daos

import com.google.inject.Inject
import models.AdminAuth
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{Completed, MongoCollection, MongoDatabase, _}
import types.TokenId

import scala.concurrent.{ExecutionContext, Future}

case class AdminAuthDAO @Inject()(mathbotDb: MongoDatabase)(implicit ec: ExecutionContext) {
  final val collectionLabel = "adminauth"

  val codecRegistry: CodecRegistry = fromRegistries(
    fromProviders(
      Macros.createCodecProvider[AdminAuth](),
      Macros.createCodecProvider[AdminAuth]()
    ),
    DEFAULT_CODEC_REGISTRY
  )

  val collection: MongoCollection[AdminAuth] =
    mathbotDb.getCollection[AdminAuth](collectionLabel).withCodecRegistry(codecRegistry)

  def insert(adminAuth: AdminAuth): Future[Option[Completed]] = {
    collection.insertOne(adminAuth).toFutureOption()
  }

  def find(adminAuthId: String): Future[Option[AdminAuth]] = {
    collection.find(equal("adminAuthId", adminAuthId)).first().toFutureOption()
  }

  def findByTokenId(tokenId: TokenId): Future[Option[AdminAuth]] = {
    collection.find(equal("tokenId", tokenId)).first().toFutureOption()
  }

  def delete(adminAuthId: String): Future[Option[DeleteResult]] = {
    collection.deleteOne(equal("adminAuthId", adminAuthId)).toFutureOption()
  }
}

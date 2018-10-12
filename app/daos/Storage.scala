package daos

import loggers.SemanticLog
import org.bson.{BsonReader, BsonSerializationException, BsonType, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries, CodecRegistry}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.hashed
import org.mongodb.scala.model.Updates.{combine, set}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Failure

abstract class Storage[Key, Value](
    val collectionLabel: Symbol,
    val keyField: Symbol,
    val valueField: Symbol,
    keyCodecFactory: CodecRegistry => Codec[Key],
    valueCodecFactory: CodecRegistry => Codec[Value],
    val codecProviders: Seq[CodecProvider] = Seq.empty[CodecProvider],
    val codecs: Seq[Codec[_]] = Seq.empty[Codec[_]]
)(implicit val ec: ExecutionContext) {

  case class KeyValue(
      _id: BsonObjectId,
      key: Key,
      value: Value
  )

  private lazy val keyCodec = keyCodecFactory(defaultCodecRegistry)
  private lazy val valueCodec = valueCodecFactory(defaultCodecRegistry)

  // A hand rolled encoder is needed because the mongodb codec registry isn't implemented to work with type parameters.
  protected class KeyValueCodec extends Codec[KeyValue] {
    override def encode(writer: BsonWriter, keyValue: KeyValue, encoderContext: EncoderContext): Unit = {
      keyValue match {
        case kv: KeyValue =>
          writer.writeStartDocument()
          writer.writeObjectId("_id", kv._id.getValue)
          writer.writeName(keyField.name)
          encoderContext.encodeWithChildContext(keyCodec, writer, kv.key)
          writer.writeName(valueField.name)
          encoderContext.encodeWithChildContext(valueCodec, writer, kv.value)
          writer.writeEndDocument()
        case _ =>
          throw new BsonSerializationException(
            s"Collection $collectionLabel encode the key value pair because it is not based on the expected key/value types"
          )
      }
    }

    override def getEncoderClass: Class[KeyValue] = classOf[KeyValue]

    override def decode(reader: BsonReader, decoderContext: DecoderContext): KeyValue = {
      var _idOpt = Option.empty[BsonObjectId]
      var keyOpt = Option.empty[Key]
      var valueOpt = Option.empty[Value]

      reader.readStartDocument()

      @tailrec
      def read(): Unit = {
        if (reader.getCurrentBsonType != BsonType.END_OF_DOCUMENT) {
          reader.readName() match {
            case "_id" => _idOpt = Some(BsonObjectId.apply(reader.readObjectId()))
            case name: String if name == keyField.name =>
              keyOpt = Some(decoderContext.decodeWithChildContext[Key](keyCodec, reader))
            case name: String if name == valueField.name =>
              valueOpt = Some(decoderContext.decodeWithChildContext[Value](valueCodec, reader))
          }
          read()
        }
      }

      read()
      reader.readEndDocument()

      (_idOpt, keyOpt, valueOpt) match {
        case (Some(_id), Some(key), Some(value)) => KeyValue(_id, key, value)
        case _ =>
          throw new BsonSerializationException(
            s"Unable to decode document from collection $collectionLabel because fields were missing."
          )
      }
    }
  }

  protected val db: MongoDatabase
  protected val logger: SemanticLog

  protected val defaultCodecRegistry: CodecRegistry = fromRegistries(
    fromProviders(
      codecProviders: _*
    ),
    CodecRegistries.fromCodecs(codecs: _*),
    DEFAULT_CODEC_REGISTRY
  )

  private val internalCodecRegistry = fromRegistries(
    defaultCodecRegistry,
    CodecRegistries.fromCodecs(keyCodec, valueCodec, new KeyValueCodec)
  )

  protected val collection: MongoCollection[KeyValue] =
    db.getCollection[KeyValue](collectionLabel.name).withCodecRegistry(internalCodecRegistry)

  def prepare() {
    collection.createIndex(hashed(keyField.name)).toFuture().onComplete {
      case Failure(t) =>
        logger.error(SemanticLog.tags.exception(t, s"Unable to add index to $collectionLabel collection"))
      case _ =>
    }
  }

  def insertOrUpdate(key: Key, value: Value): Future[Boolean] = {
    // Had to do it this way because findOneAndUpdate didn't seem to do upsert properly. At least this lets me hash sessionId
    for {
      findResult <- collection.find(equal(keyField.name, key.toString)).first().toFutureOption()
      result <- findResult match {
        case Some(prev) =>
          collection
            .updateOne(equal("_id", prev._id), combine(set(valueField.name, value)))
            .map(ur => ur.wasAcknowledged())
            .toFuture()
        case None =>
          collection.insertOne(KeyValue(BsonObjectId(), key, value)).map(_ => true).toFuture()
      }
    } yield {
      result.head
    }
  }

  def find(sessionId: Key): Future[Option[Value]] =
    for {
      result <- collection.find(equal(keyField.name, sessionId)).toFuture()
    } yield {
      result.map(_.value).headOption
    }

  def delete(sessionId: Key): Future[Boolean] =
    for {
      result <- collection.deleteOne(equal(keyField.name, sessionId.toString)).toFuture()
    } yield {
      result.getDeletedCount > 0
    }
}

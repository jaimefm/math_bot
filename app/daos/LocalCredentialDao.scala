package daos

import com.google.inject.Inject
import daos.codecs.SecureIdentifierCodec
import loggers.SemanticLog
import models.LocalCredential
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.codecs.Macros
import utils.SecureIdentifier
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.{ExecutionContext, Future}

class LocalCredentialDao @Inject()(
    override val db: MongoDatabase,
    aLogger: SemanticLog,
    aCodecs: Seq[Codec[_]] = Seq.empty[Codec[_]],
    aProviders: Seq[CodecProvider]
)(override implicit val ec: ExecutionContext)
    extends Storage[SecureIdentifier, LocalCredential](
      'localCredential,
      'accountId,
      valueField = 'credential,
      _ => new SecureIdentifierCodec,
      registry => Macros.createCodec[LocalCredential](registry),
      aProviders,
      aCodecs
    ) {

  final val usernameField = s"${valueField.name}.username"

  override protected val logger: SemanticLog = aLogger.withClass[LocalCredentialDao]()

  override def prepare() = {
    super.prepare()
    collection.createIndex(ascending(usernameField))
  }

  def find(username: String): Future[Option[LocalCredential]] =
    for {
      result <- collection.find(equal(usernameField, username)).toFuture
    } yield {
      result.map(_.value).headOption
    }
}

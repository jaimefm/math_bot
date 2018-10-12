package utils

import java.security.SecureRandom
import java.util.Base64

import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import play.api.libs.json.{Format, JsError, JsSuccess}
import spray.json.JsonFormat

class SecureIdentifier private (aId: Seq[Byte]) {
  private val id: Seq[Byte] = aId

  def toByteArray: Array[Byte] = id.toArray

  override def toString: String = SecureIdentifier.encoder.encodeToString(id.to[Array])

  override def equals(obj: scala.Any): Boolean =
    super.equals(obj) || (obj match {
      case that: SecureIdentifier => that.id.equals(this.id)
      case _ => false
    })

  override def hashCode(): Int = this.id.hashCode()
}

object SecureIdentifier {

  private val encoder = Base64.getUrlEncoder
  private val decoder = Base64.getUrlDecoder
  private val random = new SecureRandom

  implicit object SecureIdentifierFormat extends JsonFormat[SecureIdentifier] {
    import spray.json.{JsString, JsValue}

    override def read(json: JsValue): SecureIdentifier = json match {
      case JsString(base64String) => SecureIdentifier(base64String)
      case _ => SecureIdentifier(0) // Effectively a worthless identifier
    }

    override def write(obj: SecureIdentifier): JsValue = JsString(obj.toString)
  }

  implicit val secureIdentifierPlayFormatter: Format[SecureIdentifier] = new Format[SecureIdentifier] {
    import play.api.libs.json.{JsResult, JsString, JsValue}
    override def writes(o: SecureIdentifier): JsValue = JsString(o.toString)

    override def reads(json: JsValue): JsResult[SecureIdentifier] = json match {
      case JsString(base64String) => JsSuccess(SecureIdentifier(base64String))
      case _ => JsError("Invalid SecureIdentifier")
    }
  }

  def apply(aId: String): SecureIdentifier = new SecureIdentifier(decoder.decode(aId).toSeq)

  def apply(size: Int): SecureIdentifier = {
    val buffer = Array.fill[Byte](size)(0)
    random.nextBytes(buffer)
    new SecureIdentifier(buffer.toSeq)
  }

  def empty = SecureIdentifier(0)

  def fromStringOpt(sessionId: String): Option[SecureIdentifier] = {
    try {
      Some(SecureIdentifier(sessionId))
    } catch {
      case _: IllegalArgumentException =>
        None
    }
  }

  class SecureIdentifierCodec extends Codec[SecureIdentifier] {
    override def encode(writer: BsonWriter, value: SecureIdentifier, encoderContext: EncoderContext): Unit =
      writer.writeString(value.toString)

    override def getEncoderClass: Class[SecureIdentifier] = classOf[SecureIdentifier]

    override def decode(reader: BsonReader, decoderContext: DecoderContext) = SecureIdentifier(reader.readString())
  }

}

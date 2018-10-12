package models

import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonInvalidOperationException, BsonReader, BsonWriter}
import play.api.libs.json._
import utils.SecureIdentifier

import scala.util.{Failure, Success, Try}

object AuthSources extends Enumeration {
  type AuthSource = Value
  val Google, Github, Local = Value

  implicit val authSourcesFormat: Format[AuthSource] = new Format[AuthSources.AuthSource] {
    override def writes(o: AuthSource): JsValue = JsString(o.toString)

    override def reads(json: JsValue): JsResult[AuthSource] = json match {
      case JsString(name) =>
        Try(AuthSources.withName(name)) match {
          case Success(value) => JsSuccess(value)
          case Failure(_) => JsError("Unable to convert to AuthSource")
        }
      case _ => JsError("Unable to convert to AuthSource")
    }
  }

  class AuthSourceCodec extends Codec[AuthSources.AuthSource] {
    override def encode(writer: BsonWriter, value: AuthSource, encoderContext: EncoderContext): Unit =
      writer.writeString(value.toString)

    override def getEncoderClass: Class[AuthSource] = classOf[AuthSource]

    override def decode(reader: BsonReader, decoderContext: DecoderContext): AuthSource = {
      val name = reader.readString()
      Try(AuthSources.withName(name)) match {
        case Success(value) => value
        case Failure(throwable) =>
          throw new BsonInvalidOperationException("Could not read BSON value into AuthSource", throwable)
      }
    }
  }
}

case class SessionAuth(sessionId: SecureIdentifier, authSource: AuthSources.AuthSource, token_id: String)

object SessionAuth {

  implicit val sessionAuthFormat: OFormat[SessionAuth] = Json.format[SessionAuth]

}

package loggers

import akka.http.scaladsl.model.{ StatusCode, Uri }
import javassist.runtime.Desc
import utils.SecureIdentifier

import scala.reflect.ClassTag

trait SemanticLog {

  def error(data: Seq[(Symbol, String)]): Unit
  def warning(data: Seq[(Symbol, String)]): Unit
  def info(data: Seq[(Symbol, String)]): Unit
  def debug(data: Seq[(Symbol, String)]): Unit

  def debug(data: (Symbol, String)): Unit = debug(Seq(data))
  def info(data: (Symbol, String)): Unit = info(Seq(data))
  def error(data: (Symbol, String)): Unit = error(Seq(data))
  def warning(data: (Symbol, String)): Unit = warning(Seq(data))
}

object SemanticLog {
  object symbols {
    final val mongo = 'mongo
    final val source = 'source
    final val message = 'message
    final val socketId = 'socketId
    final val contents = 'contents
    final val sessionId = 'sessionId
    final val outboundUri = 'outboundUri
    final val httpStatus = 'httpStatus
    final val httpBody = 'httpBody
    final val cause = 'cause
    final val kind = 'kind
    final val caseClass = 'caseClass
    final val description = 'description
    final val actorBecomes = 'actorBecomes
    final val invalidMessage = 'invalidMessage
    final val classTag = 'classTag
    final val oauth = 'oauth
    final val collection = 'collection
    final val field = 'field
  }

  object tags {

    def `class`[T](classTag: ClassTag[T]): (Symbol, String) = symbols.classTag -> classTag.runtimeClass.getCanonicalName
    def actorBecomes(name: String) = Seq(symbols.actorBecomes -> name)
    def description(d: String) : (Symbol, String) = symbols.description -> d
    def exception(c: Throwable, d: String) = Seq(cause(c), description(d))
    def cause(c: Throwable) : (Symbol, String) = symbols.cause -> c.toString
    def mongo(sessionId: SecureIdentifier, status: String): Seq[(Symbol, String)] =
      Seq(symbols.sessionId -> sessionId.toString, symbols.mongo -> status)
    def outboundHttp(sessionId: SecureIdentifier, uri: Uri, status: StatusCode, body: String) =
      Seq(symbols.outboundUri -> uri.toString(), symbols.httpStatus -> status.value, symbols.httpBody -> body)
    def oauth(oauthSource: String, desc: String) = Seq(symbols.oauth -> oauthSource, description(desc))
    def message(msg: Any): Seq[(Symbol, String)] = Seq(symbols.message -> msg.toString)
    def index(col : Symbol, fld : Symbol, desc : String) = Seq(symbols.collection -> col.name, symbols.field -> fld.name) :+ description(desc)
    def index(col : Symbol, fld : Symbol, c: Throwable, desc : String) = Seq(symbols.collection -> col.name, symbols.field -> fld.name) ++ exception(c, desc)
  }

  implicit class loggerExtensions(innerLog: SemanticLog) {
    def withClass[T]()(implicit classTag: ClassTag[T]): SemanticLog = new ClassTagDecorator[T](innerLog)(classTag)
  }

  class ClassTagDecorator[T](var innerLog: SemanticLog)(implicit classTag: ClassTag[T]) extends SemanticLog {
    override def error(data: Seq[(Symbol, String)]): Unit = innerLog.error(data :+ tags.`class`(classTag))
    override def warning(data: Seq[(Symbol, String)]): Unit = innerLog.warning(data :+ tags.`class`(classTag))
    override def info(data: Seq[(Symbol, String)]): Unit = innerLog.info(data :+ tags.`class`(classTag))
    override def debug(data: Seq[(Symbol, String)]): Unit = innerLog.debug(data :+ tags.`class`(classTag))
  }

}

package dotty.tools
package dotc
package reporting
package diagnostic

import util.SourcePosition

import messages._

object Message {
  /** This implicit conversion provides a fallback for error messages that have
    * not yet been ported to the new scheme. Comment out this `implicit def` to
    * see where old errors still exist
    */
  implicit def toNoExplanation(str: String): Message =
    new NoExplanation(str)
}

/** A `Message` contains all semantic information necessary to easily
  * comprehend what caused the message to be logged. Each message can be turned
  * into a `MessageContainer` which contains the log level and can later be
  * consumed by a subclass of `Reporter`. However, the error position is only
  * part of `MessageContainer`, not `Message`.
  *
  * NOTE: you should not be persisting  Most messages take an implicit
  * `Context` and these contexts weigh in at about 4mb per instance, as such
  * persisting these will result in a memory leak.
  *
  * Instead use the `persist` method to create an instance that does not keep a
  * reference to these contexts.
  *
  * @param errorId a unique id identifying the message, this will later be
  *                used to reference documentation online
  */
abstract class Message(val errorId: ErrorMessageID) { self =>

  /** The `msg` contains the diagnostic message e.g:
    *
    * > expected: String
    * > found:    Int
    *
    * This message will be placed underneath the position given by the enclosing
    * `MessageContainer`
    */
  def msg: String

  /** The kind of the error message is something like "Syntax" or "Type
    * Mismatch"
    */
  def kind: String

  /** The explanation should provide a detailed description of why the error
    * occurred and use examples from the user's own code to illustrate how to
    * avoid these errors.
    */
  def explanation: String

  /** The implicit `Context` in messages is a large thing that we don't want
    * persisted. This method gets around that by duplicating the message
    * without the implicit context being passed along.
    */
  def persist: Message = new Message (errorId) {
    val msg         = self.msg
    val kind        = self.kind
    val explanation = self.explanation
  }

  def append(suffix: => String): Message = new Message(errorId) {
    val msg         = self.msg ++ suffix
    val kind        = self.kind
    val explanation = self.explanation
  }
}

/** An extended message keeps the contained message from being evaluated, while
  * allowing for extension for the `msg` string
  *
  * This is useful when we need to add additional information to an existing
  * message.
  */
class ExtendMessage(_msg: () => Message)(f: String => String) { self =>
  lazy val msg: String = f(_msg().msg)
  lazy val kind: String = _msg().kind
  lazy val explanation: String = _msg().explanation
  lazy val errorId: ErrorMessageID = _msg().errorId

  private def toMessage = new Message(errorId) {
    val msg = self.msg
    val kind = self.kind
    val explanation = self.explanation
  }

  /** Enclose this message in an `Error` container */
  def error(pos: SourcePosition): Error =
    new Error(toMessage, pos)

  /** Enclose this message in an `Warning` container */
  def warning(pos: SourcePosition): Warning =
    new Warning(toMessage, pos)

  /** Enclose this message in an `Info` container */
  def info(pos: SourcePosition): Info =
    new Info(toMessage, pos)

  /** Enclose this message in an `FeatureWarning` container */
  def featureWarning(pos: SourcePosition): FeatureWarning =
    new FeatureWarning(toMessage, pos)

  /** Enclose this message in an `UncheckedWarning` container */
  def uncheckedWarning(pos: SourcePosition): UncheckedWarning =
    new UncheckedWarning(toMessage, pos)

  /** Enclose this message in an `DeprecationWarning` container */
  def deprecationWarning(pos: SourcePosition): DeprecationWarning =
    new DeprecationWarning(toMessage, pos)

  /** Enclose this message in an `MigrationWarning` container */
  def migrationWarning(pos: SourcePosition): MigrationWarning =
    new MigrationWarning(toMessage, pos)
}

/** The fallback `Message` containing no explanation and having no `kind` */
class NoExplanation(val msg: String) extends Message(ErrorMessageID.NoExplanationID) {
  val explanation: String = ""
  val kind: String = ""

  override def toString(): String = s"NoExplanation($msg)"
}

/** The extractor for `NoExplanation` can be used to check whether any error
  * lacks an explanation
  */
object NoExplanation {
  def unapply(m: Message): Option[Message] =
    if (m.explanation == "") Some(m)
    else None
}

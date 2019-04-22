package org.perftester.sbtbot.server

import com.dhpcs.jsonrpc._
import play.api.libs.json._

sealed trait Message

sealed trait ServerCommand extends Message

sealed trait ClientCommand extends Message

sealed trait Response extends Message

sealed trait ResultResponse extends Response

sealed trait Notification extends Message

/**
  * Parameters and types used in the `initialize` message.
  */
case class InitializeParams(
                            /**
                              * The process Id of the parent process that started
                              * the server.
                              */
                            processId: Long,
                            /**
                              * The rootPath of the workspace. Is null
                              * if no folder is open.
                              */
                            rootPath: String,
                            /**
                              * The capabilities provided by the client (editor)
                              */
                            capabilities: ClientCapabilities)
    extends ServerCommand

case class InitializeError(retry: Boolean)

case class ClientCapabilities()

object ClientCapabilities {
  implicit val format: Format[ClientCapabilities] =
    Format(Reads(jsValue => JsSuccess(ClientCapabilities())), Writes(c => Json.obj()))
}

case class ServerCapabilities(
    /**
      * Defines how text documents are synced.
      */
    textDocumentSync: Int = TextDocumentSyncKind.Full,
    /**
      * The server provides hover support.
      */
    hoverProvider: Boolean = false,
    /**
      * The server provides completion support.
      */
    completionProvider: Option[CompletionOptions],
    /**
      * The server provides signature help support.
      */
    signatureHelpProvider: Option[SignatureHelpOptions] = None,
    /**
      * The server provides goto definition support.
      */
    definitionProvider: Boolean = false,
    /**
      * The server provides find references support.
      */
    referencesProvider: Boolean = false,
    /**
      * The server provides document highlight support.
      */
    documentHighlightProvider: Boolean = false,
    /**
      * The server provides document symbol support.
      */
    documentSymbolProvider: Boolean = false,
    /**
      * The server provides workspace symbol support.
      */
    workspaceSymbolProvider: Boolean = false,
    /**
      * The server provides code actions.
      */
    codeActionProvider: Boolean = false,
    /**
      * The server provides code lens.
      */
    codeLensProvider: Option[CodeLensOptions] = None,
    /**
      * The server provides document formatting.
      */
    documentFormattingProvider: Boolean = false,
    /**
      * The server provides document range formatting.
      */
    documentRangeFormattingProvider: Boolean = false,
    /**
      * The server provides document formatting on typing.
      */
    documentOnTypeFormattingProvider: Option[DocumentOnTypeFormattingOptions] = None,
    /**
      * The server provides rename support.
      */
    renameProvider: Boolean = false
)

object ServerCapabilities {
  implicit val format = Json.format[ServerCapabilities]
}

case class CompletionOptions(resolveProvider: Boolean, triggerCharacters: Seq[String])

object CompletionOptions {
  implicit val format: Format[CompletionOptions] =
    Json.format[CompletionOptions]
}

case class SignatureHelpOptions(triggerCharacters: Seq[String])

object SignatureHelpOptions {
  implicit val format: Format[SignatureHelpOptions] =
    Json.format[SignatureHelpOptions]
}

case class CodeLensOptions(resolveProvider: Boolean = false)

object CodeLensOptions {
  implicit val format: Format[CodeLensOptions] = Json.format[CodeLensOptions]
}

case class DocumentOnTypeFormattingOptions(firstTriggerCharacter: String,
                                           moreTriggerCharacters: Seq[String])

object DocumentOnTypeFormattingOptions {
  implicit val format: Format[DocumentOnTypeFormattingOptions] =
    Json.format[DocumentOnTypeFormattingOptions]
}

case class InitializeResult(capabilities: ServerCapabilities) extends ResultResponse

case class Shutdown() extends ServerCommand

object Shutdown {
  implicit val format: Format[Shutdown] =
    OFormat(Reads(jsValue => JsSuccess(Shutdown())), OWrites[Shutdown](s => Json.obj()))
}

case class ShutdownResult(dummy: Int) extends ResultResponse

case class ShowMessageRequestParams(
                                    /**
                                      * The message type. @see MessageType
                                      */
                                    tpe: Long,
                                    /**
                                      * The actual message
                                      */
                                    message: String,
                                    /**
                                      * The message action items to present.
                                      */
                                    actions: Seq[MessageActionItem])
    extends ClientCommand

/**
  * A short title like 'Retry', 'Open Log' etc.
  */
case class MessageActionItem(title: String)

object MessageActionItem {
  implicit val format = Json.format[MessageActionItem]
}

object ServerCommand extends CommandCompanion[ServerCommand] {

  import JsonRpcUtils._

  override val CommandFormats = Message.MessageFormats(
    "initialize" -> Json.format[InitializeParams],
    "shutdown"   -> Shutdown.format,
  )
}

object ClientCommand extends CommandCompanion[ClientCommand] {
  override val CommandFormats =
    Message.MessageFormats("window/showMessageRequest" -> Json.format[ShowMessageRequestParams])
}

///////////////////////////// Notifications ///////////////////////////////

// From server to client

case class ShowMessageParams(tpe: Long, message: String) extends Notification

case class LogMessageParams(tpe: Long, message: String) extends Notification

// from client to server

case class ExitNotification() extends Notification

case class DidChangeWatchedFiles(changes: Seq[FileEvent]) extends Notification

case class Initialized() extends Notification

object Initialized {
  implicit val format: Format[Initialized] =
    OFormat(Reads(jsValue => JsSuccess(Initialized())), OWrites[Initialized](s => Json.obj()))
}

case class CancelRequest(id: Int) extends Notification

case class FileEvent(uri: String, `type`: Int)

object FileEvent {
  implicit val format = Json.format[FileEvent]
}

object FileChangeType {
  final val Created = 1
  final val Changed = 2
  final val Deleted = 3
}

object Notification extends NotificationCompanion[Notification] {
  override val NotificationFormats = Message.MessageFormats(
    "window/showMessage"              -> Json.format[ShowMessageParams],
    "window/logMessage"               -> Json.format[LogMessageParams],
    "workspace/didChangeWatchedFiles" -> Json.format[DidChangeWatchedFiles],
    "initialized"                     -> Initialized.format,
    "$/cancelRequest"                 -> Json.format[CancelRequest]
  )
}

object ResultResponse extends ResponseCompanion[Any] {
  override val ResponseFormats = Message.MessageFormats(
    "initialize" -> Json.format[InitializeResult],
    "shutdown"   -> Json.format[ShutdownResult])
}

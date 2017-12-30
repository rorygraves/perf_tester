package org.perftester.sbtbot.server

import java.io.OutputStream

import akka.util.ByteString
import play.api.libs.json._

/**
  * A class to write Json RPC messages on an output stream, following the Language Server Protocol.
  * It produces the following format:
  *
  * <Header> '\r\n' <Content>
  *
  * Header := FieldName ':' FieldValue '\r\n'
  *
  * Currently there are two defined header fields:
  * - 'Content-Length' in bytes (required)
  * - 'Content-Type' (string), defaults to 'application/vscode-jsonrpc; charset=utf8'
  *
  * @note The header part is defined to be ASCII encoded, while the content part is UTF8.
  */
class MessageWriter {
  private val ContentLen = "Content-Length"

  /** Lock protecting the output stream, so multiple writes don't mix message chunks. */
  private val lock = new Object

  //  /**
  //   * Write a message to the output stream. This method can be called from multiple threads,
  //   * but it may block waiting for other threads to finish writing.
  //   */
  //  def write[T](msg: T, h: Map[String, String] = Map.empty)(implicit o: Format[T]): Unit = lock.synchronized {
  //    require(h.get(ContentLen).isEmpty)
  //
  //    val str = Json.stringify(o.writes(msg))
  //    val contentBytes = str.getBytes(MessageReader.Utf8Charset)
  //    val headers = (h + (ContentLen -> contentBytes.length))
  //      .map { case (k, v) => s"$k: $v" }
  //      .mkString("", "\r\n", "\r\n\r\n")
  //
  //    println(s"$headers\n\n$str")
  //    println(s"payload: $str")
  //
  //    val headerBytes = headers.getBytes(MessageReader.AsciiCharset)
  //
  //    out.write(headerBytes)
  //    out.write(contentBytes)
  //    out.flush()
  //  }

  /**
    * Write a message to the output stream. This method can be called from multiple threads,
    * but it may block waiting for other threads to finish writing.
    */
  def writeByteString[T](msg: T, h: Map[String, String] = Map.empty)(implicit o: Format[T]): ByteString = lock.synchronized {
    require(h.get(ContentLen).isEmpty)

    //    val str = Json.stringify(o.writes(msg)) + "\r\n"
    val str =
      """{ "jsonrpc": "2.0", "id": 1, "method": "initialize", "params": { "initializationOptions": { "token": "84046191245433876643612047032303751629" } } }\r\n"""
    //    val str = """{ "jsonrpc": "2.0", "id": 2, "method": "sbt/exec", "params": { "commandLine": "clean" } }\r\n"""
    val contentBytes = str.getBytes(MessageReader.Utf8Charset)
    val headers = (h + (ContentLen -> contentBytes.length))
      .map { case (k, v) => s"$k: $v" }
      .mkString("", "\r\n", "\r\n\r\n")

    //    println(s"$headers\n\n$str")
    println(s"payload: $str")

    //    val headerBytes = headers.getBytes(MessageReader.AsciiCharset)

    /*ByteString(headerBytes) ++ */ ByteString(contentBytes)
  }

}

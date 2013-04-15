/*
 * Copyright 2012 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cherry.scala.io

import java.io.IOException

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class CsvException(msg: String) extends IOException(msg)

/**
 * CSVパーサ.<br>
 * CSV形式データを解析して、レコード (文字列のリスト) 単位で取得する。サポートするCSV形式は RFC 4180
 * (http://www.ietf.org/rfc/rfc4180.txt) を基本とする。ただし、下記の点が RFC 4180 と異なる (RFC
 * 4180 の上位互換)。
 * <ul>
 * <li>文字データ (TEXTDATA) の範囲はUnicode (Javaが文字として扱うもの) とする。(RFC 4180
 * はASCIIの範囲に限定している)</li>
 * <li>引用無しフィールド (non-escaped) は、データ内に引用符 (DQUOTE)
 * を含んでもエラーとしない。引用符も文字データの1文字と同じように扱う。</li>
 * <li>LF, CRLF, CRCRLF, ... を一つの改行として扱う。(RFC 4180 はCRLFを改行とする)</li>
 * <li>データの最後 (end of file) はLFが無くてもエラーとはしない。(引用データ (escaped) 中を除く)</li>
 * </ul>
 */
class CsvParser(source: Source) extends Iterator[Array[String]] with CsvState {

  private var currentRecord: Option[Either[String, Array[String]]] = null

  def hasNext: Boolean =
    ensureRecord match {
      case Some(Right(_)) =>
        true
      case None =>
        false
      case Some(Left(err)) =>
        throw new CsvException(err)
    }

  def next(): Array[String] =
    ensureRecord match {
      case Some(Right(record)) =>
        currentRecord = null
        record
      case None =>
        throw new IllegalStateException("No CSV record")
      case Some(Left(err)) =>
        throw new CsvException(err)
    }

  private def ensureRecord(): Option[Either[String, Array[String]]] = {
    currentRecord = if (currentRecord == null) read() else currentRecord
    currentRecord
  }

  /**
   * CSVレコード読取り.<br>
   * データ読取り元からCSVデータを1レコード読取る。
   *
   * @return CSVデータの1レコード。
   * @throws CsvException
   *             CSV形式不正。
   */
  def read(): Option[Either[String, Array[String]]] =
    readMain(RecordBeginState, new StringBuilder, new ArrayBuffer[String])

  /**
   * CSVレコード読取りの処理本体.
   */
  private def readMain(
    curState: State,
    field: StringBuilder,
    record: ArrayBuffer[String]): Option[Either[String, Array[String]]] =
    if (curState == RecordEndState)
      if (record.isEmpty) None else Some(Right(record.toArray))
    else {
      val ch = if (source.hasNext) source.next() else -1
      curState(ch) match {
        case (ActionNone, nextState) =>
          readMain(
            nextState,
            field,
            record)
        case (ActionAppend, nextState) =>
          readMain(
            nextState,
            field + ch.toChar,
            record)
        case (ActionFlush, nextState) =>
          readMain(
            nextState,
            new StringBuilder,
            record += field.toString)
        case (_, _) =>
          Some(Left("Invalid CSV format"))
      }
    }

  /**
   * データ読取り元をクローズする.<br>
   */
  def close() = source.close()

}

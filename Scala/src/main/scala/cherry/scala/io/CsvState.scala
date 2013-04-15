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

/**
 * CSV解析用状態遷移マシン.<br>
 * CSV形式データを解析する状態遷移マシンを提供する。サポートするCSV形式は RFC 4180
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
trait CsvState {

  /**
   * 状態遷移機械における「アクション」を表す。
   */
  sealed trait Action
  object ActionNone extends Action
  object ActionAppend extends Action
  object ActionFlush extends Action
  object ActionError extends Action

  /**
   * 状態遷移機械における「状態」を表す。
   */
  sealed trait State {
    def apply(ch: Int): (Action, State)
  }

  /** 状態: RECORD_BEGIN */
  object RecordBeginState extends State {
    def apply(ch: Int) = ch match {
      case ',' => (ActionFlush, FieldBeginState)
      case '"' => (ActionNone, EscapedState)
      case '\r' => (ActionFlush, CrState)
      case '\n' => (ActionFlush, RecordEndState)
      case -1 => (ActionNone, RecordEndState)
      case _ => (ActionAppend, NonEscapedState)
    }
  }

  /** 状態: FIELD_BEGIN */
  private object FieldBeginState extends State {
    def apply(ch: Int) = ch match {
      case ',' => (ActionFlush, FieldBeginState)
      case '"' => (ActionNone, EscapedState)
      case '\r' => (ActionFlush, CrState)
      case '\n' => (ActionFlush, RecordEndState)
      case -1 => (ActionFlush, RecordEndState)
      case _ => (ActionAppend, NonEscapedState)
    }
  }

  /** 状態: NONESCAPED */
  private object NonEscapedState extends State {
    def apply(ch: Int) = ch match {
      case ',' => (ActionFlush, FieldBeginState)
      case '"' => (ActionAppend, NonEscapedState)
      case '\r' => (ActionFlush, CrState)
      case '\n' => (ActionFlush, RecordEndState)
      case -1 => (ActionFlush, RecordEndState)
      case _ => (ActionAppend, NonEscapedState)
    }
  }

  /** 状態: ESCAPED */
  private object EscapedState extends State {
    def apply(ch: Int) = ch match {
      case ',' => (ActionAppend, EscapedState)
      case '"' => (ActionNone, DquoteState)
      case '\r' => (ActionAppend, EscapedState)
      case '\n' => (ActionAppend, EscapedState)
      case -1 => (ActionError, null)
      case _ => (ActionAppend, EscapedState)
    }
  }

  /** 状態: DQUOTE */
  private object DquoteState extends State {
    def apply(ch: Int) = ch match {
      case ',' => (ActionFlush, FieldBeginState)
      case '"' => (ActionAppend, EscapedState)
      case '\r' => (ActionFlush, CrState)
      case '\n' => (ActionFlush, RecordEndState)
      case -1 => (ActionFlush, RecordEndState)
      case _ => (ActionError, null)
    }
  }

  /** 状態: CR */
  private object CrState extends State {
    def apply(ch: Int) = ch match {
      case ',' => (ActionError, null)
      case '"' => (ActionError, null)
      case '\r' => (ActionNone, CrState)
      case '\n' => (ActionNone, RecordEndState)
      case -1 => (ActionNone, RecordEndState)
      case _ => (ActionError, null)
    }
  }

  /** 状態: RECORD_END */
  object RecordEndState extends State {
    def apply(ch: Int) = (ActionNone, null)
  }

}

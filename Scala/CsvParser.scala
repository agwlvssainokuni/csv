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

import java.io.Reader

class CsvException(message: String) extends java.io.IOException(message)

class CsvParser(reader: Reader) {

	def read(): Array[String] =
		read_main(RECORD_BEGIN, new StringBuilder, List[String]())

	private type State = Int=>Trans

	private def read_main(state: State,
			field: StringBuilder,
			record: List[String]): Array[String] = state match {
		case RECORD_END =>
			if (record.isEmpty) null else record.reverse.toArray
		case _ => {
			val ch = reader.read()
			val trans = state(ch)
			trans.action match {
				case 'APPEND => read_main(trans.state,
					field + ch.asInstanceOf[Char],
					record)
				case 'FLUSH => read_main(trans.state,
					new StringBuilder,
					field.toString :: record)
				case 'NONE => read_main(trans.state,
					field,
					record)
				case 'ERROR =>
					throw new CsvException("Invalid CSV format")
				case _ => read_main(trans.state,
					field,
					record)
			}
		}
	}

	private class Trans(act: Symbol, sta: State) {
		val action: Symbol = act
		val state: State = sta
	}

	/** 状態: RECORD_BEGIN */
	private val RECORD_BEGIN: State =
		(ch: Int) => ch match {
			case ','  => new Trans('FLUSH,  FIELD_BEGIN)
			case '"'  => new Trans('NONE,   ESCAPED)
			case '\r' => new Trans('FLUSH,  CR)
			case '\n' => new Trans('FLUSH,  RECORD_END)
			case -1   => new Trans('NONE,   RECORD_END)
			case _    => new Trans('APPEND, NONESCAPED)
		}

	/** 状態: FIELD_BEGIN */
	private val FIELD_BEGIN: State =
		(ch: Int) => ch match {
			case ','  => new Trans('FLUSH,  FIELD_BEGIN)
			case '"'  => new Trans('NONE,   ESCAPED)
			case '\r' => new Trans('FLUSH,  CR)
			case '\n' => new Trans('FLUSH,  RECORD_END)
			case -1   => new Trans('FLUSH,  RECORD_END)
			case _    => new Trans('APPEND, NONESCAPED)
		}

	/** 状態: NONESCAPED */
	private val NONESCAPED: State =
		(ch: Int) => ch match {
			case ','  => new Trans('FLUSH,  FIELD_BEGIN)
			case '"'  => new Trans('APPEND, NONESCAPED)
			case '\r' => new Trans('FLUSH,  CR)
			case '\n' => new Trans('FLUSH,  RECORD_END)
			case -1   => new Trans('FLUSH,  RECORD_END)
			case _    => new Trans('APPEND, NONESCAPED)
		}

	/** 状態: ESCAPED */
	private val ESCAPED: State =
		(ch: Int) => ch match {
			case ','  => new Trans('APPEND, ESCAPED)
			case '"'  => new Trans('NONE,   DQUOTE)
			case '\r' => new Trans('APPEND, ESCAPED)
			case '\n' => new Trans('APPEND, ESCAPED)
			case -1   => new Trans('ERROR,  null)
			case _    => new Trans('APPEND, ESCAPED)
		}

	/** 状態: DQUOTE */
	private val DQUOTE: State =
		(ch: Int) => ch match {
			case ','  => new Trans('FLUSH,  FIELD_BEGIN)
			case '"'  => new Trans('APPEND, ESCAPED)
			case '\r' => new Trans('FLUSH,  CR)
			case '\n' => new Trans('FLUSH,  RECORD_END)
			case -1   => new Trans('FLUSH,  RECORD_END)
			case _    => new Trans('ERROR,  null)
		}

	/** 状態: CR */
	private val CR: State =
		(ch: Int) => ch match {
			case ','  => new Trans('ERROR,  null)
			case '"'  => new Trans('ERROR,  null)
			case '\r' => new Trans('NONE,   CR)
			case '\n' => new Trans('NONE,   RECORD_END)
			case -1   => new Trans('NONE,   RECORD_END)
			case _    => new Trans('ERROR,  null)
		}

	/** 状態: RECORD_END */
	private val RECORD_END: State =
		(ch: Int) => null

}
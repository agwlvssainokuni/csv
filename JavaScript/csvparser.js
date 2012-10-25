/*
 * Copyright 2012 Norio Agawa
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

function CsvParser(getchar) {
	this.getchar = getchar;
}

CsvParser.prototype = {

	read_record : function() {
		var record = null;
		var field = "";
		var state = this.RECORD_BEGIN;
		while (state != this.RECORD_END) {
			var ch = this.getchar();
			var trans = state.call(this, ch);
			switch (trans[0]) {
			case this.APPEND:
				field = field.concat(ch);
				break;
			case this.FLUSH:
				if (record === null) {
					record = [];
				}
				record.push(field);
				field = "";
				break;
			case this.ERROR:
				throw "Invalid CSV format";
			}
			state = trans[1];
		}
		return record;
	},

	NONE : 0,
	APPEND : 1,
	FLUSH : 2,
	ERROR : 3,

	RECORD_BEGIN : function(ch) {
		switch (ch) {
		case ",":
			return [ this.FLUSH, this.FIELD_BEGIN ];
		case "\"":
			return [ this.NONE, this.ESCAPED ];
		case "\r":
			return [ this.FLUSH, this.CR ];
		case "\n":
			return [ this.FLUSH, this.RECORD_END ];
		case null:
			return [ this.NONE, this.RECORD_END ];
		default:
			return [ this.APPEND, this.NONESCAPED ];
		}
	},

	FIELD_BEGIN : function(ch) {
		switch (ch) {
		case ",":
			return [ this.FLUSH, this.FIELD_BEGIN ];
		case "\"":
			return [ this.NONE, this.ESCAPED ];
		case "\r":
			return [ this.FLUSH, this.CR ];
		case "\n":
			return [ this.FLUSH, this.RECORD_END ];
		case null:
			return [ this.FLUSH, this.RECORD_END ];
		default:
			return [ this.APPEND, this.NONESCAPED ];
		}
	},

	NONESCAPED : function(ch) {
		switch (ch) {
		case ",":
			return [ this.FLUSH, this.FIELD_BEGIN ];
		case "\"":
			return [ this.APPEND, this.NONESCAPED ];
		case "\r":
			return [ this.FLUSH, this.CR ];
		case "\n":
			return [ this.FLUSH, this.RECORD_END ];
		case null:
			return [ this.FLUSH, this.RECORD_END ];
		default:
			return [ this.APPEND, this.NONESCAPED ];
		}
	},

	ESCAPED : function(ch) {
		switch (ch) {
		case ",":
			return [ this.APPEND, this.ESCAPED ];
		case "\"":
			return [ this.NONE, this.DQUOTE ];
		case "\r":
			return [ this.APPEND, this.ESCAPED ];
		case "\n":
			return [ this.APPEND, this.ESCAPED ];
		case null:
			return [ this.ERROR, null ];
		default:
			return [ this.APPEND, this.ESCAPED ];
		}
	},

	DQUOTE : function(ch) {
		switch (ch) {
		case ",":
			return [ this.FLUSH, this.FIELD_BEGIN ];
		case "\"":
			return [ this.APPEND, this.ESCAPED ];
		case "\r":
			return [ this.FLUSH, this.CR ];
		case "\n":
			return [ this.FLUSH, this.RECORD_END ];
		case null:
			return [ this.FLUSH, this.RECORD_END ];
		default:
			return [ this.ERROR, null ];
		}
	},

	CR : function(ch) {
		switch (ch) {
		case ",":
			return [ this.ERROR, null ];
		case "\"":
			return [ this.ERROR, null ];
		case "\r":
			return [ this.NONE, this.CR ];
		case "\n":
			return [ this.NONE, this.RECORD_END ];
		case null:
			return [ this.NONE, this.RECORD_END ];
		default:
			return [ this.ERROR, null ];
		}
	},

	RECORD_END : function(ch) {
		return null;
	}
};

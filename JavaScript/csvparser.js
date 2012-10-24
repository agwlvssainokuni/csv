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
			switch (trans.action) {
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
			state = trans.state;
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
			return {action: this.FLUSH,		state: this.FIELD_BEGIN};
		case "\"":
			return {action: this.NONE,		state: this.ESCAPED};
		case "\r":
			return {action: this.FLUSH,		state: this.CR};
		case "\n":
			return {action: this.FLUSH,		state: this.RECORD_END};
		case null:
			return {action: this.NONE,		state: this.RECORD_END};
		default:
			return {action: this.APPEND,	state: this.NONESCAPED};
		}
	},

	FIELD_BEGIN : function(ch) {
		switch (ch) {
		case ",":
			return {action: this.FLUSH,		state: this.FIELD_BEGIN};
		case "\"":
			return {action: this.NONE,		state: this.ESCAPED};
		case "\r":
			return {action: this.FLUSH,		state: this.CR};
		case "\n":
			return {action: this.FLUSH,		state: this.RECORD_END};
		case null:
			return {action: this.FLUSH,		state: this.RECORD_END};
		default:
			return {action: this.APPEND,	state: this.NONESCAPED};
		}
	},

	NONESCAPED : function(ch) {
		switch (ch) {
		case ",":
			return {action: this.FLUSH,		state: this.FIELD_BEGIN};
		case "\"":
			return {action: this.APPEND,	state: this.NONESCAPED};
		case "\r":
			return {action: this.FLUSH,		state: this.CR};
		case "\n":
			return {action: this.FLUSH,		state: this.RECORD_END};
		case null:
			return {action: this.FLUSH,		state: this.RECORD_END};
		default:
			return {action: this.APPEND,	state: this.NONESCAPED};
		}
	},

	ESCAPED : function(ch) {
		switch (ch) {
		case ",":
			return {action: this.APPEND,	state: this.ESCAPED};
		case "\"":
			return {action: this.NONE,		state: this.DQUOTE};
		case "\r":
			return {action: this.APPEND,	state: this.ESCAPED};
		case "\n":
			return {action: this.APPEND,	state: this.ESCAPED};
		case null:
			return {action: this.ERROR,		state: null};
		default:
			return {action: this.APPEND,	state: this.ESCAPED};
		}
	},

	DQUOTE : function(ch) {
		switch (ch) {
		case ",":
			return {action: this.FLUSH,		state: this.FIELD_BEGIN};
		case "\"":
			return {action: this.APPEND,	state: this.ESCAPED};
		case "\r":
			return {action: this.FLUSH,		state: this.CR};
		case "\n":
			return {action: this.FLUSH,		state: this.RECORD_END};
		case null:
			return {action: this.FLUSH,		state: this.RECORD_END};
		default:
			return {action: this.ERROR,		state: null};
		}
	},

	CR : function(ch) {
		switch (ch) {
		case ",":
			return {action: this.ERROR,		state: null};
		case "\"":
			return {action: this.ERROR,		state: null};
		case "\r":
			return {action: this.NONE,		state: this.CR};
		case "\n":
			return {action: this.NONE,		state: this.RECORD_END};
		case null:
			return {action: this.NONE,		state: this.RECORD_END};
		default:
			return {action: this.ERROR,		state: null};
		}
	},

	RECORD_END : function(ch) {
		return null;
	}
};

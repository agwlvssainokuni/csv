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

#include <stdlib.h>
#include "csvstate.h"

static void state_RECORD_BEGIN _((CsvState*, int));
static void state_FIELD_BEGIN _((CsvState*, int));
static void state_NONESCAPED _((CsvState*, int));
static void state_ESCAPED _((CsvState*, int));
static void state_DQUOTE _((CsvState*, int));
static void state_CR _((CsvState*, int));
static void state_RECORD_END _((CsvState*, int));

/*
 * CSV解析用の状態遷移機械を初期化する。
 */
void CsvStateInitialize(CsvState* state) {
	state->action = CSV_NONE;
	state->handler = state_RECORD_BEGIN;
}

/*
 * 一文字ずつCSVパーサの状態を遷移させる。
 */
void CsvStateNext(CsvState* state, int ch) {
	if (state->handler == NULL) {
		return;
	}
	(* state->handler)(state, ch);
}

/*
 * レコード終端判定。
 */
int CsvStateIsEndOfRecord(CsvState* state) {
	return state->handler == state_RECORD_END;
}

/*
 * 状態: RECORD_BEGIN
 */
static void state_RECORD_BEGIN(CsvState* state, int ch) {
	switch (ch) {
	case (int) ',': /* COMMA */
		state->action = CSV_FLUSH;
		state->handler = state_FIELD_BEGIN;
		break;
	case (int) '"': /* DQUOTE */
		state->action = CSV_NONE;
		state->handler = state_ESCAPED;
		break;
	case (int) '\r': /* CR */
		state->action = CSV_FLUSH;
		state->handler = state_CR;
		break;
	case (int) '\n': /* LF */
		state->action = CSV_FLUSH;
		state->handler = state_RECORD_END;
		break;
	case -1: /* EOF */
		state->action = CSV_NONE;
		state->handler = state_RECORD_END;
		break;
	default: /* TEXTDATA */
		state->action = CSV_APPEND;
		state->handler = state_NONESCAPED;
		break;
	}
}

/*
 * 状態: FIELD_BEGIN
 */
static void state_FIELD_BEGIN(CsvState* state, int ch) {
	switch (ch) {
	case (int) ',': /* COMMA */
		state->action = CSV_FLUSH;
		state->handler = state_FIELD_BEGIN;
		break;
	case (int) '"': /* DQUOTE */
		state->action = CSV_NONE;
		state->handler = state_ESCAPED;
		break;
	case (int) '\r': /* CR */
		state->action = CSV_FLUSH;
		state->handler = state_CR;
		break;
	case (int) '\n': /* LF */
		state->action = CSV_FLUSH;
		state->handler = state_RECORD_END;
		break;
	case -1: /* EOF */
		state->action = CSV_FLUSH;
		state->handler = state_RECORD_END;
		break;
	default: /* TEXTDATA */
		state->action = CSV_APPEND;
		state->handler = state_NONESCAPED;
		break;
	}
}

/*
 * 状態: NONESCAPED
 */
static void state_NONESCAPED(CsvState* state, int ch) {
	switch (ch) {
	case (int) ',': /* COMMA */
		state->action = CSV_FLUSH;
		state->handler = state_FIELD_BEGIN;
		break;
	case (int) '"': /* DQUOTE */
		state->action = CSV_APPEND;
		state->handler = state_NONESCAPED;
		break;
	case (int) '\r': /* CR */
		state->action = CSV_FLUSH;
		state->handler = state_CR;
		break;
	case (int) '\n': /* LF */
		state->action = CSV_FLUSH;
		state->handler = state_RECORD_END;
		break;
	case -1: /* EOF */
		state->action = CSV_FLUSH;
		state->handler = state_RECORD_END;
		break;
	default: /* TEXTDATA */
		state->action = CSV_APPEND;
		state->handler = state_NONESCAPED;
		break;
	}
}

/*
 * 状態: ESCAPED
 */
static void state_ESCAPED(CsvState* state, int ch) {
	switch (ch) {
	case (int) ',': /* COMMA */
		state->action = CSV_APPEND;
		state->handler = state_ESCAPED;
		break;
	case (int) '"': /* DQUOTE */
		state->action = CSV_NONE;
		state->handler = state_DQUOTE;
		break;
	case (int) '\r': /* CR */
		state->action = CSV_APPEND;
		state->handler = state_ESCAPED;
		break;
	case (int) '\n': /* LF */
		state->action = CSV_APPEND;
		state->handler = state_ESCAPED;
		break;
	case -1: /* EOF */
		state->action = CSV_ERROR;
		state->handler = NULL;
		break;
	default: /* TEXTDATA */
		state->action = CSV_APPEND;
		state->handler = state_ESCAPED;
		break;
	}
}

/*
 * 状態: DQUOTE
 */
static void state_DQUOTE(CsvState* state, int ch) {
	switch (ch) {
	case (int) ',': /* COMMA */
		state->action = CSV_FLUSH;
		state->handler = state_FIELD_BEGIN;
		break;
	case (int) '"': /* DQUOTE */
		state->action = CSV_APPEND;
		state->handler = state_ESCAPED;
		break;
	case (int) '\r': /* CR */
		state->action = CSV_FLUSH;
		state->handler = state_CR;
		break;
	case (int) '\n': /* LF */
		state->action = CSV_FLUSH;
		state->handler = state_RECORD_END;
		break;
	case -1: /* EOF */
		state->action = CSV_FLUSH;
		state->handler = state_RECORD_END;
		break;
	default: /* TEXTDATA */
		state->action = CSV_ERROR;
		state->handler = NULL;
		break;
	}
}

/*
 * 状態: CR
 */
static void state_CR(CsvState* state, int ch) {
	switch (ch) {
	case (int) ',': /* COMMA */
		state->action = CSV_ERROR;
		state->handler = NULL;
		break;
	case (int) '"': /* DQUOTE */
		state->action = CSV_ERROR;
		state->handler = NULL;
		break;
	case (int) '\r': /* CR */
		state->action = CSV_NONE;
		state->handler = state_CR;
		break;
	case (int) '\n': /* LF */
		state->action = CSV_NONE;
		state->handler = state_RECORD_END;
		break;
	case -1: /* EOF */
		state->action = CSV_NONE;
		state->handler = state_RECORD_END;
		break;
	default: /* TEXTDATA */
		state->action = CSV_ERROR;
		state->handler = NULL;
		break;
	}
}

/*
 * 状態: RECORD_END
 */
static void state_RECORD_END(CsvState* state, int ch) {
	state->action = CSV_NONE;
	state->handler = state_RECORD_END;
}

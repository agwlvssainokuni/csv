/*
 * Copyright 2017 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License"
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

package gocsv

import (
	"fmt"
	"io"
)

type RecordReader interface {
	ReadRecord() ([][]byte, error)
}

func NewCsvRecordReader(r io.ByteReader) RecordReader {
	return &csvParser{r: r}
}

type csvParser struct {
	r io.ByteReader
}

func (p *csvParser) ReadRecord() ([][]byte, error) {

	var record [][]byte
	field := make([]byte, 0, 16)

	state := state_RECORD_BEGIN
	for state != state_RECORD_END {

		ch, err := p.r.ReadByte()
		if err != nil && err != io.EOF {
			return nil, err
		}

		var rn rune
		if err == io.EOF {
			rn = -1
		} else {
			rn = rune(ch)
		}

		var action csvAction
		action, state = stateHandler[state](rn)
		switch action {
		case action_APPEND:
			field = append(field, ch)
		case action_FLUSH:
			if record == nil {
				record = make([][]byte, 0, 16)
			}
			record = append(record, field)
			field = make([]byte, 0, 16)
		case action_ERROR:
			return nil, fmt.Errorf("Invalid CSV format")
		}
	}

	return record, nil
}

type csvAction int

const (
	action_NONE csvAction = iota
	action_APPEND
	action_FLUSH
	action_ERROR
)

type csvState int

const (
	state_RECORD_BEGIN csvState = iota
	state_FIELD_BEGIN
	state_NONESCAPED
	state_ESCAPED
	state_DQUOTE
	state_CR
	state_RECORD_END
	state_ERROR
)

var stateHandler [7]func(rune) (csvAction, csvState)

func init() {
	stateHandler[state_RECORD_BEGIN] = handler_RECORD_BEGIN
	stateHandler[state_FIELD_BEGIN] = handler_FIELD_BEGIN
	stateHandler[state_NONESCAPED] = handler_NONESCAPED
	stateHandler[state_ESCAPED] = handler_ESCAPED
	stateHandler[state_DQUOTE] = handler_DQUOTE
	stateHandler[state_CR] = handler_CR
	stateHandler[state_RECORD_END] = handler_RECORD_END
}

/** 状態: RECORD_BEGIN */
func handler_RECORD_BEGIN(ch rune) (csvAction, csvState) {
	switch ch {
	case ',': // COMMA
		return action_FLUSH, state_FIELD_BEGIN
	case '"': // DQUOTE
		return action_NONE, state_ESCAPED
	case '\r': // CR
		return action_FLUSH, state_CR
	case '\n': // LF
		return action_FLUSH, state_RECORD_END
	case -1: // EOF
		return action_NONE, state_RECORD_END
	default: // TEXTDATA
		return action_APPEND, state_NONESCAPED
	}
}

/** 状態: FIELD_BEGIN */
func handler_FIELD_BEGIN(ch rune) (csvAction, csvState) {
	switch ch {
	case ',': // COMMA
		return action_FLUSH, state_FIELD_BEGIN
	case '"': // DQUOTE
		return action_NONE, state_ESCAPED
	case '\r': // CR
		return action_FLUSH, state_CR
	case '\n': // LF
		return action_FLUSH, state_RECORD_END
	case -1: // EOF
		return action_FLUSH, state_RECORD_END
	default: // TEXTDATA
		return action_APPEND, state_NONESCAPED
	}
}

/** 状態: NONESCAPED */
func handler_NONESCAPED(ch rune) (csvAction, csvState) {
	switch ch {
	case ',': // COMMA
		return action_FLUSH, state_FIELD_BEGIN
	case '"': // DQUOTE
		return action_APPEND, state_NONESCAPED
	case '\r': // CR
		return action_FLUSH, state_CR
	case '\n': // LF
		return action_FLUSH, state_RECORD_END
	case -1: // EOF
		return action_FLUSH, state_RECORD_END
	default: // TEXTDATA
		return action_APPEND, state_NONESCAPED
	}
}

/** 状態: ESCAPED */
func handler_ESCAPED(ch rune) (csvAction, csvState) {
	switch ch {
	case ',': // COMMA
		return action_APPEND, state_ESCAPED
	case '"': // DQUOTE
		return action_NONE, state_DQUOTE
	case '\r': // CR
		return action_APPEND, state_ESCAPED
	case '\n': // LF
		return action_APPEND, state_ESCAPED
	case -1: // EOF
		return action_ERROR, state_ERROR
	default: // TEXTDATA
		return action_APPEND, state_ESCAPED
	}
}

/** 状態: DQUOTE */
func handler_DQUOTE(ch rune) (csvAction, csvState) {
	switch ch {
	case ',': // COMMA
		return action_FLUSH, state_FIELD_BEGIN
	case '"': // DQUOTE
		return action_APPEND, state_ESCAPED
	case '\r': // CR
		return action_FLUSH, state_CR
	case '\n': // LF
		return action_FLUSH, state_RECORD_END
	case -1: // EOF
		return action_FLUSH, state_RECORD_END
	default: // TEXTDATA
		return action_ERROR, state_ERROR
	}
}

/** 状態: CR */
func handler_CR(ch rune) (csvAction, csvState) {
	switch ch {
	case ',': // COMMA
		return action_ERROR, state_ERROR
	case '"': // DQUOTE
		return action_ERROR, state_ERROR
	case '\r': // CR
		return action_NONE, state_CR
	case '\n': // LF
		return action_NONE, state_RECORD_END
	case -1: // EOF
		return action_NONE, state_RECORD_END
	default: // TEXTDATA
		return action_ERROR, state_ERROR
	}
}

/** 状態: RECORD_END */
func handler_RECORD_END(ch rune) (csvAction, csvState) {
	return action_NONE, state_RECORD_END
}

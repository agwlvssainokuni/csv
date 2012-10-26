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

#include "csvstate.h"
#include "ruby.h"

static VALUE rb_cCsvParser, rb_eCsvError;
static ID id_io, id_getc;

#define FIELD_BUFFER_LENGTH 1024

/**
 * IOオブジェクトから、CSVレコードを読取る。
 */
static VALUE read_record(VALUE io) {

	VALUE field = Qnil;
	VALUE record = Qnil;
	unsigned char fld_buff[FIELD_BUFFER_LENGTH];
	int fld_buff_len = 0;
	CsvState state;

	CsvStateInitialize(&state);

	while (1) {

		VALUE ch = rb_funcall(io, id_getc, 0);

		unsigned char ch_val;
		unsigned char* str_ptr = NULL;
		long str_len = 0L;
		if (FIXNUM_P(ch)) {
			/* Ruby 1.8 (getc returns Integer) */
			ch_val = (unsigned char) FIX2INT(ch);
			str_ptr = &ch_val;
			str_len = 1L;
		} else if (TYPE(ch) == T_STRING) {
			/* Ruby 1.9 (getc returns String) */
			str_ptr = RSTRING_PTR(ch);
			str_len = RSTRING_LEN(ch);
		}

		int ich;
		if (ch == Qnil) {
			ich = -1;
		} else {
			long i;
			ich = 0;
			for (i = 0; i < str_len; i++) {
				ich <<= 8;
				ich += (int) str_ptr[i];
			}
		}
		CsvStateNext(&state, ich);

		if (CSV_APPEND == state.action) {

			int i;
			for (i = 0; i < str_len; i++) {
				fld_buff[fld_buff_len++] = str_ptr[i];
			}

			if (fld_buff_len > FIELD_BUFFER_LENGTH - 3) {
				if (field == Qnil) {
					field = rb_str_new(fld_buff, fld_buff_len);
				} else {
					rb_str_cat(field, fld_buff, fld_buff_len);
				}
				fld_buff_len = 0;
			}
		} else if (CSV_FLUSH == state.action) {

			if (field == Qnil) {
				field = rb_str_new(fld_buff, fld_buff_len);
			} else {
				rb_str_cat(field, fld_buff, fld_buff_len);
			}
			fld_buff_len = 0;

			if (record == Qnil) {
				record = rb_ary_new();
			}

			rb_ary_push(record, field);
			field = Qnil;
		} else if (CSV_ERROR == state.action) {
			rb_raise(rb_eCsvError, "Invalid CSV format");
			return Qnil;
		} else {
			// 何もしない。
		}

		if (CsvStateIsEndOfRecord(&state)) {
			break;
		}
	}

	return record;
}

/**
 * CSVパーサオブジェクト生成。
 */
static VALUE csvparser_initialize(VALUE obj, VALUE io) {
	return rb_ivar_set(obj, id_io, io);
}

/**
 * CSVレコードを一件読取る。
 */
static VALUE csvparser_read(VALUE obj) {
	VALUE io = rb_ivar_get(obj, id_io);
	return read_record(io);
}

/**
 * CSVレコードを全件読取る。
 */
static VALUE csvparser_read_records(VALUE obj) {
	VALUE io = rb_ivar_get(obj, id_io);
	VALUE result = rb_ary_new();
	while (1) {
		VALUE record = read_record(io);
		if (record == Qnil) {
			break;
		}
		rb_ary_push(result, record);
	}
	return result;
}

/**
 * CSVレコードを順に読取りブロックに受渡す。
 */
static VALUE csvparser_each(VALUE obj) {
	VALUE io = rb_ivar_get(obj, id_io);
	while (1) {
		VALUE record = read_record(io);
		if (record == Qnil) {
			break;
		}
		rb_yield(record);
	}
	return Qnil;
}

/**
 * モジュール初期化。
 */
void Init_csvparser() {

	id_io = rb_intern("io");
	id_getc = rb_intern("getc");

	rb_cCsvParser = rb_define_class("CsvParser", rb_cObject);
	rb_eCsvError = rb_define_class("CsvError", rb_eStandardError);

	rb_define_method(rb_cCsvParser, "initialize", csvparser_initialize, 1);
	rb_define_method(rb_cCsvParser, "read", csvparser_read, 0);
	rb_define_method(rb_cCsvParser, "read_records", csvparser_read_records, 0);
	rb_define_method(rb_cCsvParser, "each", csvparser_each, 0);
	rb_define_method(rb_cCsvParser, "each_records", csvparser_each, 0);
}

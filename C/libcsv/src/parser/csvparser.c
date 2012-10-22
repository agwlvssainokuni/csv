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

#include <apr_strings.h>
#include <csvstate.h>
#include "csvparser.h"

#define FIELD_BUFFER_LENGTH 1024
#define INITIAL_RECORD_NALLOC 10

apr_status_t CsvParserReadRecord(
	apr_file_t* file,
	apr_array_header_t** record,
	apr_pool_t* pool) {

	apr_status_t status, result;
	apr_array_header_t* record_ptr = NULL;
	apr_pool_t* fld_pool = NULL;
	unsigned char* fld_temp = NULL;
	unsigned char fld_buff[FIELD_BUFFER_LENGTH];
	int fld_buff_len = 0;
	CsvState csvstate;

	// (1) 初期化する。
	// ・終了ステータスをAPR_SUCCESSに初期化
	// ・CSV状態遷移機械を初期化
	result = APR_SUCCESS;
	CsvStateInitialize(&csvstate);

	// (2) 子プールを作成する。
	status = apr_pool_create(&fld_pool, pool);
	if (APR_SUCCESS != status) {
		return status;
	}

	// (3) 以下を繰り返す (ループ)。
	while (1) {

		unsigned char ch;
		int ich;

		// (4) ファイルから1文字読み込む
		status = apr_file_getc(&ch, file);
		if (APR_SUCCESS == status) {
			ich = (int) ch;
		} else if (APR_STATUS_IS_EOF(status)) {
			ich = -1;
		} else {
			result = status;
			break;
		}

		// (5) CSV状態遷移する。
		CsvStateNext(&csvstate, ich);
		if (CSV_APPEND == csvstate.action) {

			// (6) アクションがCSV_APPENDなら、
			// ・当該文字をフィールドに追加。
			fld_buff[fld_buff_len++] = (unsigned char) ich;

			if (fld_buff_len >= FIELD_BUFFER_LENGTH - 1) {
				fld_buff[fld_buff_len++] = (unsigned char) 0;
				if (fld_temp == NULL) {
					fld_temp = apr_pstrcat(fld_pool, fld_buff, NULL);
				} else {
					fld_temp = apr_pstrcat(fld_pool, fld_temp, fld_buff, NULL);
				}
				fld_buff_len = 0;
			}
		} else if (CSV_FLUSH == csvstate.action) {

			// (7) アクションがCSV_FLUSHなら、
			// ・当該フィールドをレコードに追加。
			fld_buff[fld_buff_len++] = (unsigned char) 0;
			if (fld_temp == NULL) {
				fld_temp = apr_pstrcat(pool, fld_buff, NULL);
			} else {
				fld_temp = apr_pstrcat(pool, fld_temp, fld_buff, NULL);
				apr_pool_clear(fld_pool);
			}
			fld_buff_len = 0;

			if (NULL == record_ptr) {
				record_ptr = apr_array_make(pool,
					INITIAL_RECORD_NALLOC, sizeof(unsigned char*));
			}

			APR_ARRAY_PUSH(record_ptr, unsigned char*) = fld_temp;
			fld_temp = NULL;
		} else if (CSV_ERROR == csvstate.action) {

			// (8) アクションがCSV_ERRORなら、
			// ・終了ステータスをエラーに設定してループを終了する。
			result = APR_CSVPARSER_ERROR;
			break;
		} else {
			// 何もしない。
		}

		// (9) 1レコード読込んだら、ループを終了する。
		if (CsvStateIsEndOfRecord(&csvstate)) {
			if (NULL != record_ptr) {
				(*record) = record_ptr;
			} else {
				result = APR_EOF;
			}
			break;
		}
	}

	// (10) 子プールを破棄する。
	apr_pool_destroy(fld_pool);

	return result;
}

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

#include <stdio.h>
#include "csvparser.h"

int main(int argc, char** argv) {

	apr_status_t status;
	apr_file_t* file;
	apr_pool_t* pool;
	apr_pool_t* record_pool;
	apr_array_header_t* record;
	char error_msg[1024];

	if (argc < 2) {
		fprintf(stderr, "Usage: %s {filename}\n", argv[0]);
		return -1;
	}

	status = apr_pool_initialize();
	if (APR_SUCCESS != status) {
		goto RETURN;
	}

	status = apr_pool_create(&pool, NULL);
	if (APR_SUCCESS != status) {
		goto POOL_TERMINATE;
	}

	status = apr_pool_create(&record_pool, pool);
	if (APR_SUCCESS != status) {
		goto POOL_DESTROY;
	}

	status = apr_file_open(&file, argv[1],
		APR_READ | APR_BUFFERED,
		APR_OS_DEFAULT, pool);
	if (APR_SUCCESS != status) {
		goto POOL_DESTROY;
	}

	while (1) {

		status = CsvParserReadRecord(file, &record, record_pool);
		if (APR_SUCCESS == status) {
			int i;
			fprintf(stdout, "<R>");
			for (i = 0; i < record->nelts; i++) {
				fprintf(stdout, "<F>%s</F>",
					APR_ARRAY_IDX(record, i, unsigned char*));
			}
			fprintf(stdout, "</R>");
			apr_pool_clear(record_pool);
		} else if (APR_STATUS_IS_EOF(status)) {
			break;
		} else {
			fprintf(stdout, "error: %s\n",
				(APR_CSVPARSER_ERROR == status ? "Invalid CSV format" :
					apr_strerror(status, error_msg, 1024)));
			break;
		}
	}

	apr_file_close(file);

	POOL_DESTROY:
	apr_pool_destroy(pool);

	POOL_TERMINATE:
	apr_pool_terminate();

	RETURN:
	return status;
}

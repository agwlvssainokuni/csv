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

#include <stdio.h>
#include "csvstate.h"

int main(int argc, char** argv) {

	FILE* fp;

	if (argc < 2) {
		fprintf(stderr, "Usage: %s {filename}\n", argv[0]);
		return -1;
	}

	fp = fopen(argv[1], "r");
	if (fp == NULL) {
		fprintf(stderr, "Failed to open a file %s\n", argv[1]);
		return -1;
	}

	while (1) {
		int ret = read_record(fp);
		if (ret <= 0) {
			break;
		}
	}

	fclose(fp);

	return 0;
}

int read_record(FILE* fp) {

	CsvState state;
	int num_of_fields = 0;
	int record_label = 1;
	int field_label = 1;

	CsvStateInitialize(&state);

	while (1) {

		int ch = fgetc(fp);
		CsvStateNext(&state, ch);

		switch (state.action) {
		case CSV_APPEND:
			if (record_label) {
				fprintf(stdout, "<R>");
				record_label = 0;
			}
			if (field_label) {
				fprintf(stdout, "<F>");
				field_label = 0;
			}
			fputc(ch, stdout);
			break;
		case CSV_FLUSH:
			if (record_label) {
				fprintf(stdout, "<R>");
				record_label = 0;
			}
			if (field_label) {
				fprintf(stdout, "<F>");
				field_label = 0;
			}
			fprintf(stdout, "</F>");
			num_of_fields += 1;
			field_label = 1;
			break;
		case CSV_ERROR:
			fprintf(stderr, "error: Invalid CSV format\n");
			return -1;
		}

		if (CsvStateIsEndOfRecord(&state)) {
			if (record_label == 0) {
				fprintf(stdout, "</R>");
			}
			break;
		}
	}

	return num_of_fields;
}

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

#ifndef _CSVSTATE_H_
#define _CSVSTATE_H_

#if defined(__cplusplus)
extern "C" {
#if 0
}	/* satisfy cc-mode */
#endif
#endif

#if defined(__cplusplus)
#  define _(args) args
#else
#  define _(args) ()
#endif

typedef struct _CsvState {
	enum {
		CSV_NONE, CSV_APPEND, CSV_FLUSH, CSV_ERROR
	} action;
	void (*handler)(struct _CsvState*, int);
} CsvState;

void CsvStateInitialize _((CsvState*));
void CsvStateNext _((CsvState*, int));
int CsvStateIsEndOfRecord _((CsvState*));

#if defined(__cplusplus)
#if 0
{	/* satisfy cc-mode */
#endif
}	/* extern "C" { */
#endif

#endif	/* _CSVSTATE_H_ */

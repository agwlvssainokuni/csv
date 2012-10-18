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

#ifndef _CSVPARSER_H_
#define _CSVPARSER_H_

#include <apr_errno.h>
#include <apr_file_io.h>
#include <apr_pools.h>
#include <apr_tables.h>

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


#define APR_CSVPARSER_ERROR (APR_OS_START_USERERR + 1)

apr_status_t
CsvParserReadRecord _((apr_file_t*, apr_array_header_t**, apr_pool_t*));


#if defined(__cplusplus)
#if 0
{	/* satisfy cc-mode */
#endif
}	/* extern "C" { */
#endif

#endif	/* _CSVPARSER_H_ */

#!/bin/bash
#
#  Copyright 2012 agwlvssainokuni
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

function parse_ok {

	src=$( tempfile -d . )
	echo -ne $1 > ${src}
	expected=$( tempfile -d . )
	echo -ne $2 > ${expected}
	result=$( tempfile -d . )

	${prog} ${src} > ${result} 2>&1
	diff ${expected} ${result} > /dev/null 2>&1
	if [[ $? -eq 0 ]]; then
		echo -n "."
	else
		echo "NG: src=$1 expected=$2 result=$( cat ${result} )"
	fi

	rm -f ${src} ${expected} ${result}
}

function parse_ng {

	src=$( tempfile -d . )
	echo -ne $1 > ${src}
	result=$( tempfile -d . )

	${prog} ${src} > ${result} 2>&1
	grep "error: Invalid CSV format" ${result} > /dev/null 2>&1
	if [[ $? -eq 0 ]]; then
		echo -n "."
	else
		echo "NG: src=$1 result=$( cat ${result} )"
	fi

	rm -f ${src} ${result}
}

prog=$1

parse_ok "" ""
parse_ok "\n" "<R><F></F></R>"
parse_ok "\r\n" "<R><F></F></R>"
parse_ok "\r" "<R><F></F></R>"
parse_ok "\r\r" "<R><F></F></R>"
parse_ok "\r\r\n" "<R><F></F></R>"
parse_ok "," "<R><F></F><F></F></R>"
parse_ok ",\n" "<R><F></F><F></F></R>"
parse_ok ",\r\n" "<R><F></F><F></F></R>"
parse_ok ",\r" "<R><F></F><F></F></R>"
parse_ok ",\r\r" "<R><F></F><F></F></R>"
parse_ok ",\r\r\n" "<R><F></F><F></F></R>"
parse_ok ",,\r\n" "<R><F></F><F></F><F></F></R>"
parse_ng "\r,"
parse_ng "\r\""
parse_ng "\ra"

parse_ok "aa,bb" "<R><F>aa</F><F>bb</F></R>"
parse_ok "aa,bb\r\n" "<R><F>aa</F><F>bb</F></R>"
parse_ok "aa,bb\n" "<R><F>aa</F><F>bb</F></R>"
parse_ok "aa,bb\r\ncc,dd" \
		"<R><F>aa</F><F>bb</F></R><R><F>cc</F><F>dd</F></R>"
parse_ok "aa,bb\r\ncc,dd\r\n" \
		"<R><F>aa</F><F>bb</F></R><R><F>cc</F><F>dd</F></R>"
parse_ok "a\"a,b\"\"b\r\n" \
		"<R><F>a\"a</F><F>b\"\"b</F></R>"

parse_ok "\"aa\",\"bb\"" "<R><F>aa</F><F>bb</F></R>"
parse_ok "\"aa\",\"bb\"\r\n" "<R><F>aa</F><F>bb</F></R>"
parse_ok "\"aa\",\"bb\"\n" "<R><F>aa</F><F>bb</F></R>"
parse_ok "\"aa\",\"bb\"\r\n\"cc\",\"dd\"" \
		"<R><F>aa</F><F>bb</F></R><R><F>cc</F><F>dd</F></R>"
parse_ok "\"aa\",\"bb\"\r\n\"cc\",\"dd\"\r\n" \
		"<R><F>aa</F><F>bb</F></R><R><F>cc</F><F>dd</F></R>"
parse_ok "\"a\"\"a\",\"b,b\"\r\n\"c\rc\",\"d\nd\"\r\n" \
		"<R><F>a\"a</F><F>b,b</F></R><R><F>c\rc</F><F>d\nd</F></R>"
parse_ng "\"a\"a\""
parse_ng "\"a"

%% Copyright [yyyy] [name of copyright owner]
%%
%% Licensed under the Apache License, Version 2.0 (the "License");
%% you may not use this file except in compliance with the License.
%% You may obtain a copy of the License at
%%
%%     http://www.apache.org/licenses/LICENSE-2.0
%%
%% Unless required by applicable law or agreed to in writing, software
%% distributed under the License is distributed on an "AS IS" BASIS,
%% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%% See the License for the specific language governing permissions and
%% limitations under the License.

-module(csvparser).
-export([read/2, file/1, string/1]).


%% @spec read(Fun, Arg) => {ok, [Field, Field, ...}, NextArg}
%%                         {eof, NextArg}
read(Fun, Arg) ->
	case read(Fun, Arg, state_RECORD_BEGIN, [], []) of
		{ok, [], Arg2} -> {eof, Arg2};
		Else -> Else
	end.

read(Fun, Arg, state_RECORD_BEGIN, F, R) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2,
				state_FIELD_BEGIN,
				[],
				[lists:reverse(F)|R]);
		{ok, "\"", Arg2} -> read(Fun, Arg2,
				state_ESCAPED,
				F,
				R);
		{ok, "\r", Arg2} -> read(Fun, Arg2,
				state_CR,
				[],
				[lists:reverse(F)|R]);
		{ok, "\n", Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				[],
				[lists:reverse(F)|R]);
		{ok, [C], Arg2} -> read(Fun, Arg2,
				state_NONESCAPED,
				[C|F],
				R);
		{eof, Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				F,
				R)
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, state_FIELD_BEGIN, F, R) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2,
				state_FIELD_BEGIN,
				[],
				[lists:reverse(F)|R]);
		{ok, "\"", Arg2} -> read(Fun, Arg2,
				state_ESCAPED,
				F,
				R);
		{ok, "\r", Arg2} -> read(Fun, Arg2,
				state_CR,
				[],
				[lists:reverse(F)|R]);
		{ok, "\n", Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				[],
				[lists:reverse(F)|R]);
		{ok, [C], Arg2} -> read(Fun, Arg2,
				state_NONESCAPED,
				[C|F],
				R);
		{eof, Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				[],
				[lists:reverse(F)|R])
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, state_NONESCAPED, F, R) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2,
				state_FIELD_BEGIN,
				[],
				[lists:reverse(F)|R]);
		{ok, "\"", Arg2} -> read(Fun, Arg2,
				state_NONESCAPED,
				[$"|F],
				R);
		{ok, "\r", Arg2} -> read(Fun, Arg2,
				state_CR,
				[],
				[lists:reverse(F)|R]);
		{ok, "\n", Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				[],
				[lists:reverse(F)|R]);
		{ok, [C], Arg2} -> read(Fun, Arg2,
				state_NONESCAPED,
				[C|F],
				R);
		{eof, Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				[],
				[lists:reverse(F)|R])
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, state_ESCAPED, F, R) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2,
				state_ESCAPED,
				[$,|F],
				R);
		{ok, "\"", Arg2} -> read(Fun, Arg2,
				state_DQUOTE,
				F,
				R);
		{ok, "\r", Arg2} -> read(Fun, Arg2,
				state_ESCAPED,
				[$\r|F],
				R);
		{ok, "\n", Arg2} -> read(Fun, Arg2,
				state_ESCAPED,
				[$\n|F],
				R);
		{ok, [C], Arg2} -> read(Fun, Arg2,
				state_ESCAPED,
				[C|F],
				R);
		{eof, Arg2} -> 
				{formaterror, eof, lists:reverse(F), lists:reverse(R), Arg2}
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, state_DQUOTE, F, R) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2,
				state_FIELD_BEGIN,
				[],
				[lists:reverse(F)|R]);
		{ok, "\"", Arg2} -> read(Fun, Arg2,
				state_ESCAPED,
				[$"|F],
				R);
		{ok, "\r", Arg2} -> read(Fun, Arg2,
				state_CR,
				[],
				[lists:reverse(F)|R]);
		{ok, "\n", Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				[],
				[lists:reverse(F)|R]);
		{ok, [C], Arg2} ->
				{formaterror, [C], lists:reverse(F), lists:reverse(R), Arg2};
		{eof, Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				[],
				[lists:reverse(F)|R])
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, state_CR, F, R) ->
	try Fun(Arg) of
		{ok, ",", Arg2} ->
				{formaterror, [$,], lists:reverse(F), lists:reverse(R), Arg2};
		{ok, "\"", Arg2} ->
				{formaterror, [$"], lists:reverse(F), lists:reverse(R), Arg2};
		{ok, "\r", Arg2} -> read(Fun, Arg2,
				state_CR,
				F,
				R);
		{ok, "\n", Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				F,
				R);
		{ok, [C], Arg2} ->
				{formaterror, [C], lists:reverse(F), lists:reverse(R), Arg2};
		{eof, Arg2} -> read(Fun, Arg2,
				state_RECORD_END,
				F,
				R)
	catch
		_:Why -> {ioerror, Why}
	end;

read(_Fun, Arg, state_RECORD_END, _F, R) ->
	{ok, lists:reverse(R), Arg}.


%% @spec file(IoDevice) => {ok, [Char], IoDevice}.
%%                         {eof, IoDevice}.
file(IoDevice) ->
	case io:get_chars(IoDevice, "", 1) of
		[Char] ->
			{ok, [Char], IoDevice};
		eof ->
			{eof, IoDevice}
	end.

%% @spec string(String) => {ok, [Char], RestOfString}.
%%                         {eof, []}
string([Char|Str]) -> {ok, [Char], Str};
string([]) -> {eof, []}.

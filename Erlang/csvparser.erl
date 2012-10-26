%% Copyright 2012 agwlvssainokuni
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
	case read(Fun, Arg, [], [], state_RECORD_BEGIN) of
		{ok, [], Arg2} -> {eof, Arg2};
		Else -> Else
	end.

read(Fun, Arg, F, R, state_RECORD_BEGIN) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2, F, R, $,,
				action_FLUSH,
				state_FIELD_BEGIN);
		{ok, "\"", Arg2} -> read(Fun, Arg2, F, R, $",
				action_NONE,
				state_ESCAPED);
		{ok, "\r", Arg2} -> read(Fun, Arg2, F, R, $\r,
				action_FLUSH,
				state_CR);
		{ok, "\n", Arg2} -> read(Fun, Arg2, F, R, $\n,
				action_FLUSH,
				state_RECORD_END);
		{ok, [C], Arg2} -> read(Fun, Arg2, F, R, C,
				action_APPEND,
				state_NONESCAPED);
		{eof, Arg2} -> read(Fun, Arg2, F, R, eof,
				action_NONE,
				state_RECORD_END)
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, F, R, state_FIELD_BEGIN) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2, F, R, $,,
				action_FLUSH,
				state_FIELD_BEGIN);
		{ok, "\"", Arg2} -> read(Fun, Arg2, F, R, $",
				action_NONE,
				state_ESCAPED);
		{ok, "\r", Arg2} -> read(Fun, Arg2, F, R, $\r,
				action_FLUSH,
				state_CR);
		{ok, "\n", Arg2} -> read(Fun, Arg2, F, R, $\n,
				action_FLUSH,
				state_RECORD_END);
		{ok, [C], Arg2} -> read(Fun, Arg2, F, R, C,
				action_APPEND,
				state_NONESCAPED);
		{eof, Arg2} -> read(Fun, Arg2, F, R, eof,
				action_FLUSH,
				state_RECORD_END)
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, F, R, state_NONESCAPED) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2, F, R, $,,
				action_FLUSH,
				state_FIELD_BEGIN);
		{ok, "\"", Arg2} -> read(Fun, Arg2, F, R, $",
				action_APPEND,
				state_NONESCAPED);
		{ok, "\r", Arg2} -> read(Fun, Arg2, F, R, $\r,
				action_FLUSH,
				state_CR);
		{ok, "\n", Arg2} -> read(Fun, Arg2, F, R, $\n,
				action_FLUSH,
				state_RECORD_END);
		{ok, [C], Arg2} -> read(Fun, Arg2, F, R, C,
				action_APPEND,
				state_NONESCAPED);
		{eof, Arg2} -> read(Fun, Arg2, F, R, eof,
				action_FLUSH,
				state_RECORD_END)
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, F, R, state_ESCAPED) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2, F, R, $,,
				action_APPEND,
				state_ESCAPED);
		{ok, "\"", Arg2} -> read(Fun, Arg2, F, R, $",
				action_NONE,
				state_DQUOTE);
		{ok, "\r", Arg2} -> read(Fun, Arg2, F, R, $\r,
				action_APPEND,
				state_ESCAPED);
		{ok, "\n", Arg2} -> read(Fun, Arg2, F, R, $\n,
				action_APPEND,
				state_ESCAPED);
		{ok, [C], Arg2} -> read(Fun, Arg2, F, R, C,
				action_APPEND,
				state_ESCAPED);
		{eof, Arg2} -> read(Fun, Arg2, F, R, eof,
				action_ERROR,
				state_ERROR)
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, F, R, state_DQUOTE) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2, F, R, $,,
				action_FLUSH,
				state_FIELD_BEGIN);
		{ok, "\"", Arg2} -> read(Fun, Arg2, F, R, $",
				action_APPEND,
				state_ESCAPED);
		{ok, "\r", Arg2} -> read(Fun, Arg2, F, R, $\r,
				action_FLUSH,
				state_CR);
		{ok, "\n", Arg2} -> read(Fun, Arg2, F, R, $\n,
				action_FLUSH,
				state_RECORD_END);
		{ok, [C], Arg2} -> read(Fun, Arg2, F, R, C,
				action_ERROR,
				state_ERROR);
		{eof, Arg2} -> read(Fun, Arg2, F, R, eof,
				action_FLUSH,
				state_RECORD_END)
	catch
		_:Why -> {ioerror, Why}
	end;

read(Fun, Arg, F, R, state_CR) ->
	try Fun(Arg) of
		{ok, ",", Arg2} -> read(Fun, Arg2, F, R, $,,
				action_ERROR,
				state_ERROR);
		{ok, "\"", Arg2} -> read(Fun, Arg2, F, R, $",
				action_ERROR,
				state_ERROR);
		{ok, "\r", Arg2} -> read(Fun, Arg2, F, R, $\r,
				action_NONE,
				state_CR);
		{ok, "\n", Arg2} -> read(Fun, Arg2, F, R, $\n,
				action_NONE,
				state_RECORD_END);
		{ok, [C], Arg2} -> read(Fun, Arg2, F, R, C,
				action_ERROR,
				state_ERROR);
		{eof, Arg2} -> read(Fun, Arg2, F, R, eof,
				action_NONE,
				state_RECORD_END)
	catch
		_:Why -> {ioerror, Why}
	end;

read(_Fun, Arg, _F, R, state_RECORD_END) ->
	{ok, lists:reverse(R), Arg}.

read(Fun, Arg, F, R, _C, action_NONE, State) ->
	read(Fun, Arg, F, R, State);
read(Fun, Arg, F, R, C, action_APPEND, State) ->
	read(Fun, Arg, [C|F], R, State);
read(Fun, Arg, F, R, _C, action_FLUSH, State) ->
	read(Fun, Arg, [], [lists:reverse(F)|R], State);
read(_Fun, Arg, F, R, C, action_ERROR, _State) ->
	case C of
		eof ->
			{formaterror, eof, lists:reverse(F), lists:reverse(R), Arg};
		Ch ->
			{formaterror, [Ch], lists:reverse(F), lists:reverse(R), Arg}
	end.


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

#!/usr/bin/escript
%%
%% Copyright 2012 Norio Agawa
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
%%

main([ARGV]) ->
	case file:open(ARGV, read) of
		{ok, IoDev} ->
			each_record(fun(D) -> csvparser:file(D) end, IoDev),
			file:close(IoDev);
		{error, Why} ->
			io:format("failed to open ~s (reason: ~p)~n", [ARGV, Why])
	end.

each_record(Fun, Arg) ->
	case csvparser:read(Fun, Arg) of
		{ok, R, Arg2} ->
			io:format("<record>\n"),
			print_field(R),
			io:format("</record>\n"),
			each_record(Fun, Arg2);
		{eof, Arg2} ->
			{eof, Arg2};
		Other ->
			io:format("failed to parse: ~p~n", [Other])
	end.

print_field([F|R]) ->
	io:format("<field>~s</field>~n", [F]),
	print_field(R);
print_field([]) ->
	ok.

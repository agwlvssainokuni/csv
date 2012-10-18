# coding: utf-8
#
# Copyright 2012 Norio Agawa
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

class CsvParser

	def initialize(io)
		@io = io
	end

	def read
		record = nil
		field = ""
		state = @@RECORD_BEGIN
		until state == @@RECORD_END
			ch = @io.getc
			trans = state.call(ch)
			case trans[:action]
			when :APPEND
				field << ch
			when :FLUSH
				record = [] if record.nil?
				record << field
				field = ""
			when :ERROR
				raise CsvError, "failed to parse"
			end
			state = trans[:state]
		end
		record
	end

	def read_records
		ret = []
		until (rec = read).nil?
			ret << rec
		end
		ret
	end

	def each
		until (rec = read).nil?
			yield rec
		end
	end

	alias :each_records :each

	@@RECORD_BEGIN = lambda {|ch|
		case ch
		when ","	# COMMA
			{:action => :FLUSH,  :state => @@FIELD_BEGIN}
		when "\""	# DQUOTE
			{:action => :NONE,   :state => @@ESCAPED}
		when "\r"	# CR
			{:action => :FLUSH,  :state => @@CR}
		when "\n"	# LF
			{:action => :FLUSH,  :state => @@RECORD_END}
		when nil	# EOF
			{:action => :NONE,   :state => @@RECORD_END}
		else		# TEXTDATA
			{:action => :APPEND, :state => @@NONESCAPED}
		end
	}

	@@FIELD_BEGIN = lambda {|ch|
		case ch
		when ","	# COMMA
			{:action => :FLUSH,  :state => @@FIELD_BEGIN}
		when "\""	# DQUOTE
			{:action => :NONE,   :state => @@ESCAPED}
		when "\r"	# CR
			{:action => :FLUSH,  :state => @@CR}
		when "\n"	# LF
			{:action => :FLUSH,  :state => @@RECORD_END}
		when nil	# EOF
			{:action => :FLUSH,  :state => @@RECORD_END}
		else		# TEXTDATA
			{:action => :APPEND, :state => @@NONESCAPED}
		end
	}

	@@NONESCAPED = lambda {|ch|
		case ch
		when ","	# COMMA
			{:action => :FLUSH,  :state => @@FIELD_BEGIN}
		when "\""	# DQUOTE
			{:action => :APPEND, :state => @@NONESCAPED}
		when "\r"	# CR
			{:action => :FLUSH,  :state => @@CR}
		when "\n"	# LF
			{:action => :FLUSH,  :state => @@RECORD_END}
		when nil	# EOF
			{:action => :FLUSH,  :state => @@RECORD_END}
		else		# TEXTDATA
			{:action => :APPEND, :state => @@NONESCAPED}
		end
	}

	@@ESCAPED = lambda {|ch|
		case ch
		when ","	# COMMA
			{:action => :APPEND, :state => @@ESCAPED}
		when "\""	# DQUOTE
			{:action => :NONE,   :state => @@DQUOTE}
		when "\r"	# CR
			{:action => :APPEND, :state => @@ESCAPED}
		when "\n"	# LF
			{:action => :APPEND, :state => @@ESCAPED}
		when nil	# EOF
			{:action => :ERROR,  :state => nil}
		else		# TEXTDATA
			{:action => :APPEND, :state => @@ESCAPED}
		end
	}

	@@DQUOTE = lambda {|ch|
		case ch
		when ","	# COMMA
			{:action => :FLUSH,  :state => @@FIELD_BEGIN}
		when "\""	# DQUOTE
			{:action => :APPEND, :state => @@ESCAPED}
		when "\r"	# CR
			{:action => :FLUSH,  :state => @@CR}
		when "\n"	# LF
			{:action => :FLUSH,  :state => @@RECORD_END}
		when nil	# EOF
			{:action => :FLUSH,  :state => @@RECORD_END}
		else		# TEXTDATA
			{:action => :ERROR,  :state => nil}
		end
	}

	@@CR = lambda {|ch|
		case ch
		when ","	# COMMA
			{:action => :ERROR,  :state => nil}
		when "\""	# DQUOTE
			{:action => :ERROR,  :state => nil}
		when "\r"	# CR
			{:action => :NONE,   :state => @@CR}
		when "\n"	# LF
			{:action => :NONE,   :state => @@RECORD_END}
		when nil	# EOF
			{:action => :NONE,   :state => @@RECORD_END}
		else		# TEXTDATA
			{:action => :ERROR,  :state => nil}
		end
	}

	@@RECORD_END = lambda {|ch|
		nil
	}

end

class CsvError < StandardError
end

#!/usr/bin/ruby
#
#  Copyright 2012 Norio Agawa
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
#

require './csvparser'

puts "CsvParser#read"
File.open(ARGV[0]) {|io|
	csv = CsvParser.new(io)
	until (r = csv.read).nil?
		puts "<record>"
		r.each {|f| puts "<field>#{f}</field>" }
		puts "</record>"
	end
}

puts
puts "CsvParser#read_records"
File.open(ARGV[0]) {|io|
	CsvParser.new(io).read_records.each {|r|
		puts "<record>"
		r.each {|f| puts "<field>#{f}</field>" }
		puts "</record>"
	}
}

puts
puts "CsvParser#each"
File.open(ARGV[0]) {|io|
	CsvParser.new(io).each {|r|
		puts "<record>"
		r.each {|f| puts "<field>#{f}</field>" }
		puts "</record>"
	}
}

puts
puts "CsvParser#each_records"
File.open(ARGV[0]) {|io|
	CsvParser.new(io).each_records {|r|
		puts "<record>"
		r.each {|f| puts "<field>#{f}</field>" }
		puts "</record>"
	}
}

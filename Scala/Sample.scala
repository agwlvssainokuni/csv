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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

object Sample {

	def main(args: Array[String]) {
		val reader: Reader = new BufferedReader(new FileReader(args(0)))
		try {
			val parser = new CsvParser(reader)
			read_loop(parser.read, parser)
		} catch {
			case ex: CsvParser => println("error: " + ex.getMessage)
		} finally {
			reader.close()
		}
	}

	def read_loop(record: Array[String], parser: CsvParser) {
		if (record != null) {
			print("<R>")
			for (field <- record)  {
				print("<F>")
				print(field)
				print("</F>")
			}
			print("</R>")
			read_loop(parser.read, parser)
		}
	}

}

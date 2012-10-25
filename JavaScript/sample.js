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

importPackage(java.io);
importPackage(java.lang);

function main(args) {

	var reader = new BufferedReader(new FileReader(args[0]));
	try {

		var getchar = function() {
			var ch = reader.read();
			if (ch < 0) {
				return null;
			}
			return String.fromCharCode(ch);
		};

		var parser = new CsvParser(getchar);
		var record;
		while ((record = parser.read_record()) != null) {
			print("<R>");
			for ( var i = 0; i < record.length; i++) {
				print("<F>");
				print(record[i]);
				print("</F>");
			}
			print("</R>");
		}

	} catch (ex) {
		System.err.println("error: " + ex);
	} finally {
		reader.close();
	}
}

main(arguments);
System.exit(0);

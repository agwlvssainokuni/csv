/*
 *   Copyright 2012 agwlvssainokuni
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package cherry.java.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * CSVパーサコマンドライン実行サンプル.
 */
public class Sample {

	/**
	 * CSVパーサコマンドライン実行サンプル.
	 *
	 * @param args
	 *            コマンドライン引数
	 */
	public static void main(String[] args) throws IOException {

		CsvParser parser = new CsvParser(new BufferedReader(new FileReader(
				args[0])));
		try {
			List<String> record;
			while ((record = parser.read()) != null) {
				System.out.print("<R>");
				for (String field : record) {
					System.out.print("<F>");
					System.out.print(field);
					System.out.print("</F>");
				}
				System.out.print("</R>");
			}
		} catch (CsvException ex) {
			System.err.println("error: " + ex.getMessage());
		} finally {
			parser.close();
		}
	}

}

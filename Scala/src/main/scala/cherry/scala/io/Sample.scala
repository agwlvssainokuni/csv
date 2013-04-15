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

package cherry.scala.io

import scala.io.Source

/**
 * CSVパーサコマンドライン実行サンプル.
 */
object Sample extends App {
  val parser: CsvParser = new CsvParser(Source.fromFile(args(0)))
  try {
    for (record <- parser) {
      print("<R>")
      print(record.mkString("<F>", "</F><F>", "</F>"))
      print("</R>")
    }
  } catch {
    case ex: CsvException => println("error: " + ex.getMessage)
  } finally {
    parser.close()
  }
}

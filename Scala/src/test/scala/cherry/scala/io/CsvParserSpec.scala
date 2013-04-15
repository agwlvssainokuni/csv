/*
 * Copyright 2013 agwlvssainokuni
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

import org.specs2.mutable.Specification

/**
 * {@link CsvParser}の試験.
 */
class CsvParserSpec extends Specification {

  "CsvParser#read" should {
    /**
     * <dl>
     * <dt>テストID</dt>
     * <dd>CASE001</dd>
     * <dt>テスト内容</dt>
     * <dd>
     * 空文字列 ("") を解析する。</dd>
     * </dl>
     */
    "CASE001" in {
      withCsvParser("") { parser =>
        parser.read() must beNone
        parser.read() must beNone
      }
    }

    /**
     * <dl>
     * <dt>テストID</dt>
     * <dd>CASE002</dd>
     * <dt>テスト内容</dt>
     * <dd>
     * 改行のみ ([LF]) を解析する。</dd>
     * </dl>
     */
    "CASE002" in {
      withCsvParser("\n") { parser =>
        parser.read() must beSome.which { v =>
          val Right(record) = v
          record === Array("")
        }
        parser.read() must beNone
      }
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE003</dd>
   * <dt>テスト内容</dt>
   * <dd>
   * 改行のみ ([CR][LF]) を解析する。</dd>
   * </dl>
   */
  "CASE003" in {
    withCsvParser("\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE004</dd>
   * <dt>テスト内容</dt>
   * <dd>
   * 改行のみ ([CR]) を解析する。</dd>
   * </dl>
   */
  "CASE004" in {
    withCsvParser("\r") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE005</dd>
   * <dt>テスト内容</dt>
   * <dd>
   * 改行のみ ([CR][CR]) を解析する。</dd>
   * </dl>
   */
  "CASE005" in {
    withCsvParser("\r\r") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE006</dd>
   * <dt>テスト内容</dt>
   * <dd>
   * 改行のみ ([CR][CR][LF]) を解析する。</dd>
   * </dl>
   */
  "CASE006" in {
    withCsvParser("\r\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE007</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * ,
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE007" in {
    withCsvParser(",") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("", "")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE008</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * ,[LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE008" in {
    withCsvParser(",\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("", "")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE009</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * ,[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE009" in {
    withCsvParser(",\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("", "")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE010</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * ,[CR]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE010" in {
    withCsvParser(",\r") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("", "")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE011</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * ,[CR][CR]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE011" in {
    withCsvParser(",\r\r") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("", "")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE012</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * ,[CR][CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE012" in {
    withCsvParser(",\r\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("", "")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE013</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * ,,[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE013" in {
    withCsvParser(",,\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("", "", "")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE014</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * [CR],
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE014" in {
    withCsvParser("\r,") { parser =>
      parser.read() must beSome.which { v =>
        v must beLeft
      }
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE015</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * [CR]"
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE015" in {
    withCsvParser("\r\"") { parser =>
      parser.read() must beSome.which { v =>
        v must beLeft
      }
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE016</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * [CR]a
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE016" in {
    withCsvParser("\ra") { parser =>
      parser.read() must beSome.which { v =>
        v must beLeft
      }
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE100</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * aa,bb
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE100" in {
    withCsvParser("aa,bb") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE101</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * aa,bb[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE101" in {
    withCsvParser("aa,bb\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE102</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * aa,bb[LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE102" in {
    withCsvParser("aa,bb\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE103</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * aa,bb[CR][LF]
   * cc,dd
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE103" in {
    withCsvParser("aa,bb\r\ncc,dd") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("cc", "dd")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE104</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * aa,bb[CR][LF]
   * cc,dd[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE104" in {
    withCsvParser("aa,bb\r\ncc,dd\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("cc", "dd")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE105</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * a"a,b""b[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE105" in {
    withCsvParser("a\"a,b\"\"b\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("a\"a", "b\"\"b")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE200</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "aa","bb"
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE200" in {
    withCsvParser("\"aa\",\"bb\"") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE201</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "aa","bb"[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE201" in {
    withCsvParser("\"aa\",\"bb\"\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE202</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "aa","bb"[LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE202" in {
    withCsvParser("\"aa\",\"bb\"\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE203</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "aa","bb"[CR][LF]
   * "cc","dd"
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE203" in {
    withCsvParser("\"aa\",\"bb\"\r\ncc,dd") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("cc", "dd")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE204</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "aa","bb"[CR][LF]
   * "cc","dd"[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE204" in {
    withCsvParser("\"aa\",\"bb\"\r\ncc,dd\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("aa", "bb")
      }
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("cc", "dd")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE205</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "a""a","b,b"[CR][LF]
   * "c[CR]c","d[LF]d"[CR][LF]
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE205" in {
    withCsvParser("\"a\"\"a\",\"b,b\"\r\n\"c\rc\",\"d\nd\"\r\n") { parser =>
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("a\"a", "b,b")
      }
      parser.read() must beSome.which { v =>
        val Right(record) = v
        record === Array("c\rc", "d\nd")
      }
      parser.read() must beNone
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE206</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "a"a"
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE206" in {
    withCsvParser("\"a\"a\"") { parser =>
      parser.read() must beSome.which { v =>
        v must beLeft
      }
    }
  }

  /**
   * <dl>
   * <dt>テストID</dt>
   * <dd>CASE207</dd>
   * <dt>テスト内容</dt>
   * <dd>
   *
   * <pre>
   * "a
   * </pre>
   *
   * </dd>
   * </dl>
   */
  "CASE207" in {
    withCsvParser("\"a") { parser =>
      parser.read() must beSome.which { v =>
        v must beLeft
      }
    }
  }

  def withCsvParser[T](data: String)(f: CsvParser => T) = {
    val parser = new CsvParser(Source.fromString(data))
    try {
      f(parser)
    } finally {
      parser.close()
    }
  }
}

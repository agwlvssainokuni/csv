CSVパーサ
=========

概要
----
各種言語によるCSVパーサ実装。

内訳
----
* Java
  * https://github.com/agwlvssainokuni/cherry.git のcherry-utilプロジェクト。
* C言語
  * CSV解析の状態遷移機械。
  * CSVパーサ (APRでメモリ管理)。
* Ruby
  * C言語版の状態遷移機械を使用した拡張ライブラリ。
  * ピュアRuby版CSVパーサ。
* Erlang
* Scheme
  * R5RS + SRFI-6 の機能を使用してパーサ実装。
    * 処理系にはGaucheを選択して確認。

サンプルで性能測定
------------------
* 実施内容
  * 対象: 郵便番号データ (全369,995件; kogaki + oogaki + roman)
  * OS: Ubuntu Server 12.04.1 LTS
  * CPU: Core i5 2.3GHz * 2 (VMware Player)
  * MEM: 2GB
* Java
  * real    0m11.684s
  * user    0m9.397s
  * sys     0m2.556s
* C言語
  * 状態遷移機械
    * real    0m1.504s
    * user    0m1.428s
    * sys     0m0.052s
    * 表示のみ (フィールド＆レコードのメモリ割当なし)
  * パーサ (APR版)
    * real    0m2.820s
    * user    0m2.752s
    * sys     0m0.036s
    * ファイルバッファなしだと
      * real    0m11.895s
      * user    0m2.764s
      * sys     0m9.045s
* Ruby
  * 拡張ライブラリ版
    * real    0m9.287s
    * user    0m9.073s
    * sys     0m0.084s
  * ピュアRuby版
    * real    0m57.252s
    * user    0m56.880s
    * sys     0m0.116s
* Erlang
  * インタプリタ実行
    * real    4m4.105s
    * user    3m55.159s
    * sys     0m8.273s
  * コンパイル実行 (escript -c)
    * real    3m8.216s
    * user    3m0.847s
    * sys     0m6.912s
* Scheme (R5RS + SRFI-6)
  * Gauche (R5RS + SRFI-6)
    * real    0m18.738s
    * user    0m18.645s
    * sys     0m0.048s

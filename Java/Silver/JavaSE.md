- [Javaファイルのコンパイル](#javaファイルのコンパイル)
    - [javacコマンドのオプション](#javacコマンドのオプション)
    - [javaコマンドのオプション](#javaコマンドのオプション)
    - [jdepsコマンド](#jdepsコマンド)
- [Javaでのリテラル](#javaでのリテラル)
  - [整数リテラル](#整数リテラル)
  - [浮動小数点数リテラル](#浮動小数点数リテラル)
  - [数値リテラルのアンダースコア\_](#数値リテラルのアンダースコア_)
  - [文字リテラル](#文字リテラル)
  - [文字列リテラル](#文字列リテラル)
  - [論理値リテラル](#論理値リテラル)
  - [nullリテラル](#nullリテラル)
- [変数名(識別子)のルール](#変数名識別子のルール)
- [staticイニシャライザ](#staticイニシャライザ)
- [インスタンス化されたオブジェクトが何なのかに注意しよう](#インスタンス化されたオブジェクトが何なのかに注意しよう)
- [要注意！落とし穴リスト](#要注意落とし穴リスト)
  - [varはローカル変数にしか使用できない。](#varはローカル変数にしか使用できない)
  - [varは初期値を指定しなければならない。nullはダメ](#varは初期値を指定しなければならないnullはダメ)
  - [配列を初期化して宣言するときは、要素数は省略する。](#配列を初期化して宣言するときは要素数は省略する)
  - [配列で初期化しなかった場合のデフォルト初期値](#配列で初期化しなかった場合のデフォルト初期値)
  - [演算前の暗黙型変換5パターン](#演算前の暗黙型変換5パターン)
  - [Stringクラスの文字列操作は、処理結果を戻り値で受け取る。](#stringクラスの文字列操作は処理結果を戻り値で受け取る)
  - [StringBuilderの文字列操作は、オブジェクトの値が直接書き換わる。](#stringbuilderの文字列操作はオブジェクトの値が直接書き換わる)
  - [StringBuilder sb.delete(2, 5) とすると、削除されるのは2番目の要素から4番目の要素。](#stringbuilder-sbdelete2-5-とすると削除されるのは2番目の要素から4番目の要素)
  - [+= で文字列の結合はできるが、-=で文字列を消せるということは無い。](#-で文字列の結合はできるが-で文字列を消せるということは無い)
  - [switch文の式には、double, float, longは使えません。](#switch文の式にはdouble-float-longは使えません)
  - [switch文の式で指定されている型とcase式の型が合わないとコンパイルエラーとなる。](#switch文の式で指定されている型とcase式の型が合わないとコンパイルエラーとなる)
  - [switch文の式の結果がnullの時は、コンパイルは通るがNullPointerExceptionとなる。](#switch文の式の結果がnullの時はコンパイルは通るがnullpointerexceptionとなる)
  - [continue文が使えるのは、繰り返し文(for,while)の{}の中だけ。](#continue文が使えるのは繰り返し文forwhileのの中だけ)
  - [オーバーロード時の注意①: 戻り値の型が違うだけではオーバーロードとならない。](#オーバーロード時の注意-戻り値の型が違うだけではオーバーロードとならない)
  - [オーバーロード時の注意②: オーバーロードの条件は、引数の並び、データ型、引数の数](#オーバーロード時の注意-オーバーロードの条件は引数の並びデータ型引数の数)
  - [インスタンスメソッド→staticメソッドのオーバーライドはできない。](#インスタンスメソッドstaticメソッドのオーバーライドはできない)
  - [staticメソッド→インスタンスメソッドのオーバーライドはできない。](#staticメソッドインスタンスメソッドのオーバーライドはできない)
  - [サブクラスをインスタンス化すると、暗黙的にスーパークラスのコンストラクタが呼ばれる。](#サブクラスをインスタンス化すると暗黙的にスーパークラスのコンストラクタが呼ばれる)
  - [暗黙的なスーパークラスのコンストラクタの呼び出しでは、super()が呼ばれる。](#暗黙的なスーパークラスのコンストラクタの呼び出しではsuperが呼ばれる)
  - [アクセス修飾子の公開範囲](#アクセス修飾子の公開範囲)
  - [interfaceで宣言された変数は、強制的に`public static final`となる。](#interfaceで宣言された変数は強制的にpublic-static-finalとなる)
  - [interfaceで宣言された抽象メソッドは、`public abstract`のみ。](#interfaceで宣言された抽象メソッドはpublic-abstractのみ)
  - [interfaceのデフォルトメソッドは、`public default`のみ。](#interfaceのデフォルトメソッドはpublic-defaultのみ)
  - [interfaceのstaticメソッドは、`public static`か`private static`のみ。](#interfaceのstaticメソッドはpublic-staticかprivate-staticのみ)
  - [コンパイルする時は、宣言された変数の型でチェック。実際に実行されるのは、インスタンス化された時のオブジェクト。](#コンパイルする時は宣言された変数の型でチェック実際に実行されるのはインスタンス化された時のオブジェクト)
  - [interfaceの実装クラスでは、必ず`public`を指定してオーバーライドする。](#interfaceの実装クラスでは必ずpublicを指定してオーバーライドする)
  - [interfaceの実装クラスが、abstractクラスの時は、interfaceのメソッドをオーバーライドしなくてもよい。](#interfaceの実装クラスがabstractクラスの時はinterfaceのメソッドをオーバーライドしなくてもよい)
  - [staticメソッドは、変数宣言時のメソッドが呼ばれる。](#staticメソッドは変数宣言時のメソッドが呼ばれる)
  - [サブクラスでオーバーライドした場合、サブクラスが優先して呼び出されるのはインスタンスメソッドだけ。](#サブクラスでオーバーライドした場合サブクラスが優先して呼び出されるのはインスタンスメソッドだけ)
  - [TODO:参照型での暗黙型変換について確認する。](#todo参照型での暗黙型変換について確認する)
  - [関数型インターフェースの定義](#関数型インターフェースの定義)
  - [ラムダ式 OKの記述パターンまとめ](#ラムダ式-okの記述パターンまとめ)
  - [サブクラスでメソッドをオーバーライドした時のthrowsのルール](#サブクラスでメソッドをオーバーライドした時のthrowsのルール)
  - [catch句では、例外クラスのスーパークラスから書き始めるとコンパイルエラーとなる。](#catch句では例外クラスのスーパークラスから書き始めるとコンパイルエラーとなる)
  - [private Integer i; などとして、ラッパークラスに初期値を指定せず初期化すると、nullで初期化される。](#private-integer-i-などとしてラッパークラスに初期値を指定せず初期化するとnullで初期化される)
  - [例外処理はサブクラスの例外から処理される](#例外処理はサブクラスの例外から処理される)
- [スーパークラスのメソッドにthrowsがあっても、サブクラスでthrowsを指定しなくて良い。](#スーパークラスのメソッドにthrowsがあってもサブクラスでthrowsを指定しなくて良い)

# Javaファイルのコンパイル
`javac`でコンパイル、`java`コマンドで実行する。
```bash
# 実行例
javac -d . ..\src\module-info.java ..\src\Main.java
java --module-path . --module com.seshop.sample/com.seshop.sample.main.Main
```

### javacコマンドのオプション

| オプション | 説明                                                                           |
| ---------- | ------------------------------------------------------------------------------ |
| `-d`       | パッケージ名に対応したディレクトリを作成し、その下にクラスファイルを作成する。 |


### javaコマンドのオプション
| オプション                 | 説明                                                                                                                                              |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| `-cp`, `-classpath`        | classファイルやjarファイルと一緒に実行する時に、classやjarのあるパスを指定して使う。                                                              |
| `-p`, `--module-path`      | アプリケーションやモジュールを検索する位置を指定する。フルパスで指定するのが有効。可読性を上げるため、`--module-path`を使うことも推奨されている。 |
| `-m`, `--module`           | プログラムを実行する時に、モジュール名とエントリーポイントとなるクラス(mainクラス)を指定する。`モジュール名/実行するパッケージ名`                 |
| `--describe-module`        | モジュール記述子の情報を参照する。                                                                                                                |
| `--show-module-resolution` | モジュール解決の様子を出力する。                                                                                                                  |
| `--list-modules`           | モジュールのリストを一覧で表示する。                                                                                                              |

```bash
# 利用例
javac Main.java
java -cp .:/usr/src/mysql-connector-java-8.0.12.jar Main

javac -p ../ -d . module-info.java Main.java
java -p ".:../" -m com.seshop.sample/com.seshop.sample.main.Main
```

```java
// module-info.javaの例
module com.seshop.liba {
    requires com.seshop.libc;
    // exports宣言をすることで、module-info.javaで定義したモジュールをexport先でimportすることができる。
    exports com.seshop.sample.firstpkg;
}
```

### jdepsコマンド
モジュールの依存関係を調べるコマンド。
```bash
# 実行例
jdeps myapp.jar
```


# Javaでのリテラル
- 整数リテラル
- 浮動小数点数リテラル
- 数値リテラル
- 文字リテラル
- 文字列リテラル
- 論理値リテラル
- nullリテラル

## 整数リテラル
基本的な数値は10進数として扱われるので特に躓くところはなし。<br><br>
2進数: 先頭に`0b`または`0B`を付けると2進数として扱われる。<br>
8進数: 先頭に`0`を付けると8進数として扱われる。<br>
16進数: 先頭に`0x`または`0X`を付けると16進数として扱われる。<br>

## 浮動小数点数リテラル
`12.34`はそのままの意味。`3e4`とすると、`3.0*10^4`を表す。

## 数値リテラルのアンダースコア_
数値リテラルには可読性向上のため`_`を使用できる。
ただし、

- `_`は先頭と末尾、`0x`,`0b`の前後、`F`,`L`の前後、小数点の前後には使えない。

※`float`型や`long`型に代入する時は、`1.000F`,`1000000L`のように末尾に`F`,`L`を付けなければならない。

## 文字リテラル
`'A'`や`'B'`といった1文字を表現する。<br>
`\u`のあとに4桁の16進数を指定すると、Unicode値となる。

## 文字列リテラル
`""`で囲まれたもの。文字列。特に躓くところはなし。

## 論理値リテラル
`true`, `false`を表す。特に躓くところはなし。

## nullリテラル
`null`。特に躓くところはなし。

# 変数名(識別子)のルール
1文字目･･･`a-z,A-Z`, `$`, `_`のみ<br>
2文字目以降･･･数字もOK<br>
実は識別子に`var`, `open`, `module`, `requires`, `transitive`, `exports`, `opens`, `to`, `uses`, `provides`, `with`が使える。


# staticイニシャライザ
`.class`が読み込まれたタイミングで実行されるため、インスタンス化されるよりも先に実行される。
```java
class Test {
    static {
        System.out.println("Testクラス：static イニシャライザ");
    }

    Test() {
        System.out.println("Testクラス：コンストラクタ");
    }
}

public class Main {
    static {
        System.out.println("Mainクラス：static イニシャライザ");
    }

    public static void main(String[] args) {
        System.out.println("Mainクラス：main()メソッド");
        Test obj = new Test();
    }
}
```

```
# 実行結果
Mainクラス：static イニシャライザ
Mainクラス：main()メソッド
Testクラス：static イニシャライザ
Testクラス：コンストラクタ
```

# インスタンス化されたオブジェクトが何なのかに注意しよう

# 要注意！落とし穴リスト
## varはローカル変数にしか使用できない。
## varは初期値を指定しなければならない。nullはダメ
したがって、以下のように使うのはダメ。
```java
public class Main {
    var a = 10; // メンバー変数でvarは使えない✕
    public void x() {
        var b; // 初期値を指定していないのでコンパイルエラー
        var c = null; // varにnullは代入できないのでコンパイルエラー
        var d = {10, 20}; //varを使用した配列の初期化では明示的に型を指定しなければならないのでコンパイルエラー
        var e = new ArrayList<String>(); // インスタンス変数にvarを使うのもダメ✕
    }

    public void y(var a) { // varは仮引数に指定できないのでコンパイルエラー
        //
    }
}
```

OKなパターンは下の4パターン
```java
public class Main {
    public void x() {
        var ary1 = new int[]{1, 2, 3};
        var ary2 = new int[3];
        var a = 10;
        final var b = 20;
    }
}
```

## 配列を初期化して宣言するときは、要素数は省略する。
## 配列で初期化しなかった場合のデフォルト初期値
配列の要素を初期化しなかった場合、デフォルトの初期値が割り当てられる。
| 型                                     | 初期値 |
| -------------------------------------- | ------ |
| `int[]`, `byte[]`, `long[]`, `short[]` | 0      |
| `float[]`, `double[]`                  | 0.0    |
| `char[]`                               | \u0000 |
| `boolean[]`                            | false  |
| `String[]`, その他参照型[]             | null   |


## 演算前の暗黙型変換5パターン
```
(double) + (double, float, long, int, short, char, byte)    →右辺はdouble型に変換されてから演算される。
(float)  + (float, long, int, short, char, byte)            →右辺はfloat型に変換されてから演算される。
(long)  + (long, int, short, char, byte)                    →右辺はlong型に変換されてから演算される。
(int, short, char, byte) + (int, short, char, byte)         →両辺はint型に変換されてから演算される。
(short, char, byte) + (short, char, byte)                   →これも両辺がint型に変換されてから演算される。
```
※だからchar→int は暗黙型変換となる。

## Stringクラスの文字列操作は、処理結果を戻り値で受け取る。
## StringBuilderの文字列操作は、オブジェクトの値が直接書き換わる。
## StringBuilder sb.delete(2, 5) とすると、削除されるのは2番目の要素から4番目の要素。
## += で文字列の結合はできるが、-=で文字列を消せるということは無い。
## switch文の式には、double, float, longは使えません。
逆に使えるのは以下の型です。
| switch文の式に使える型 |
| ---------------------- |
| `char`, `Character`    |
| `byte`, `Byte`         |
| `short`, `Short`       |
| `int`, `Integer`       |
| `enum`                 |
| `String`               |

## switch文の式で指定されている型とcase式の型が合わないとコンパイルエラーとなる。
## switch文の式の結果がnullの時は、コンパイルは通るがNullPointerExceptionとなる。
## continue文が使えるのは、繰り返し文(for,while)の{}の中だけ。
したがって次のような使い方はできない。
```java
public class Main {
    public static void main(String[] args) {
        int i = 2;
        Outer:
        if (i < 5) {
            System.out.println("i: " + i);
            i++;
            continue Outer; // 繰り返し文の中でないのでコンパイルエラー
        }
    }
}
```

## オーバーロード時の注意①: 戻り値の型が違うだけではオーバーロードとならない。
## オーバーロード時の注意②: オーバーロードの条件は、引数の並び、データ型、引数の数
## インスタンスメソッド→staticメソッドのオーバーライドはできない。
## staticメソッド→インスタンスメソッドのオーバーライドはできない。
## サブクラスをインスタンス化すると、暗黙的にスーパークラスのコンストラクタが呼ばれる。
したがって、スーパークラスのコンストラクタ→サブクラスのコンストラクタ という実行順序になる。

## 暗黙的なスーパークラスのコンストラクタの呼び出しでは、super()が呼ばれる。
引数付きのコンストラクタを呼び出したい場合は、明示的に`super(a);`などと記述しておくこと。

## アクセス修飾子の公開範囲
(広い)`public`>`protected`>`package`>`private`(狭い)

## interfaceで宣言された変数は、強制的に`public static final`となる。
必ずstatic変数で、finalが付くので、初期化しなければならない。

## interfaceで宣言された抽象メソッドは、`public abstract`のみ。
## interfaceのデフォルトメソッドは、`public default`のみ。
## interfaceのstaticメソッドは、`public static`か`private static`のみ。
## コンパイルする時は、宣言された変数の型でチェック。実際に実行されるのは、インスタンス化された時のオブジェクト。
## interfaceの実装クラスでは、必ず`public`を指定してオーバーライドする。
## interfaceの実装クラスが、abstractクラスの時は、interfaceのメソッドをオーバーライドしなくてもよい。
## staticメソッドは、変数宣言時のメソッドが呼ばれる。
## サブクラスでオーバーライドした場合、サブクラスが優先して呼び出されるのはインスタンスメソッドだけ。
## TODO:参照型での暗黙型変換について確認する。

## 関数型インターフェースの定義
定義されている抽象メソッドが1つだけのインターフェースを「関数型インターフェース」という。

## ラムダ式 OKの記述パターンまとめ
```java
(String str) -> { return "Hello" + str; };  // 基本形
(str) -> { return "Hello" + str; };         // 引数の型名は省略できる
str -> { return "Hello" + str; };           // 引数が一つの時は()を省略できる
str -> return "Hello" + str;                // 右辺の処理が1行の時は、{}を省略できる
str -> "Hello" + str;                       // {}が省略できるとき、returnも省略できる
() -> "Hello";                              // 引数が無いときは、空っぽの()で書く
```
関数型インターフェースは、定義されている抽象メソッドが1つだけなので、<br>
関数型インターフェースをラムダ式で実装した場合、メソッド名を指定しなくても抽象メソッドをオーバーライドした処理が呼ばれる。


## サブクラスでメソッドをオーバーライドした時のthrowsのルール
## catch句では、例外クラスのスーパークラスから書き始めるとコンパイルエラーとなる。
## private Integer i; などとして、ラッパークラスに初期値を指定せず初期化すると、nullで初期化される。
例えば、次のようなプログラムでは、変数iはnullで初期化されている。
```java
public class Main {
    private Integer i;
    public Main(int i) {
        this.i = this.i + i;　// NullPointerException
    }

    public static void main(String[] args) {
        Main obj = new Main(5);
    }
}
```

## 例外処理はサブクラスの例外から処理される
したがって、下記のプログラムで発生する例外は、`NumberFormatException`となる。
```java
class P {
    public void strToNum(String s) {
        System.out.println("P : " + Integer.parseInt(s));
    }
}

class C extends P {
    public void strToNum(String s) throws RuntimeException {
        System.out.println("C : " + Integer.parseInt(s)); // NumberFormatException
    }
}

public class Main {
    public static void main(String[] args) {
        C c = new C();
        c.strToNum("a");
    }
}
```

# スーパークラスのメソッドにthrowsがあっても、サブクラスでthrowsを指定しなくて良い。

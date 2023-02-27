# Javaのモジュールシステムについて
プログラムの最小単位をクラスとし、複数のクラスをグループ化する仕組みがパッケージである。


#### モジュールの一覧表示
```
$ java --list-modules
```

## モジュールの宣言
- モジュールの宣言は`module-info.java`というファイルで行う。
- モジュール記述子は`module-info.java`で定義したモジュール宣言をコンパイルしたもので、`module-info.class`のことを指す。<br>
全てのモジュールにおいて、java.baseは暗黙的に宣言されるので、`requires java.base;`は省略可能。
```
構文
module モジュール名 {
}
```


```:1\src\module-info.java
module com.seshop.sample {
    requires java.base;
}
```

### モジュールにクラスを配置する
`1\src\module-info.java`内で宣言した`com.seshop.sample`モジュールにMainクラスを格納する場合、以下のように行う。
この例では、Mainクラスはcom.seshop.sample.mainパッケージに属したクラスとして作成される。
```:1\src\Main.java
package com.seshop.sample.main;

public class Main {
    public static void main(String[] args) {
        System.out.println("Module System");
    }
}
```

上記の例で作成した、`Main.java`と`module-info.java`をコンパイルして生成したクラスファイルを`1\com.seshop.sample`ディレクトリ以下に配置すると、モジュールとして利用できる。

### モジュールやパッケージの依存関係の調べ方
`jdeps`コマンドを利用する。<br>
jdepsコマンドでは<b>classファイル</b>や<b>jarファイル名</b>を指定する。
<実行例>
```
\1\com.seshop.sample>jdeps com\seshop\sample\main\Main.class
Main.class -> java.base
    com.seshop.sample.main      -> java.io      java.base
    com.seshop.sample.main      -> java.lang    java.base
```

# javadoc形式でドキュメントを作成する時のコメントアウトの方法
`/** ～*/`でコメントアウトする。

- 例
```java:
/**
*   @param index 取得するデータのインデックス
*   @return 取得した個人データ
*/
public Employee getEmployee(int index)
```

### javadocの主なタグ
#### @see 参照先名
対象クラスから他クラスや関連するパッケージへの参照リンクを作成する。

#### @exception 例外クラス 説明
対象メソッドがスローする可能性がある例外の説明を記述する。

#### @param 引数名 説明
コンストラクタやメソッドの引数の説明を記述する。

#### @return 説明
対象メソッドの戻り値の説明を記述する。

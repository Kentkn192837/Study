- [クラス・メソッド・プロパティ](#クラスメソッドプロパティ)
  - [仮引数を可変長にしたい](#仮引数を可変長にしたい)
- [データベース](#データベース)
  - [データベースに接続したい](#データベースに接続したい)
- [ファイル入出力](#ファイル入出力)
  - [ファイルを読み込みたい](#ファイルを読み込みたい)
- [コレクション](#コレクション)
  - [ArrayListの利用](#arraylistの利用)
- [例外と例外処理](#例外と例外処理)
  - [独自例外クラスの定義方法](#独自例外クラスの定義方法)

# クラス・メソッド・プロパティ
## 仮引数を可変長にしたい
- データ型のあとに「...」と記述する。
- 可変長引数は最後に置く。
- 可変長引数は1つしか使用できない。
- 関数オーバーロードする場合、可変長引数を使って定義されたメソッドと引数リストがすべて明記されたメソッドでは後者のメソッドが優先して実行される。

<h3>実行例</h3>

```java:test1.java
public class Test
{
    public void method(String s, int... a)
    {
        System.out.println(s + " サイズ : " + a.length);
        for (int i : a) {
            System.out.println(" 第2引数の値 :" + i);
        }
    }
}

class Test1
{
    public static void main(String[] args)
    {
        Test obj = new Test();
        int[] ary = {10, 20, 30};
        obj.method("1回目 ");
        obj.method("2回目 ", 10);
        obj.method("3回目 ", 10, 20);
        obj.method("4回目 ", ary);
        // 下の行のコメントアウトを外すと、NullPointerException例外が発生する。
        // obj.method("5回目 ", null);
    }
}
```

<h3>実行結果</h3>

```
1回目  サイズ : 0
2回目  サイズ : 1
 第2引数の値 :10
3回目  サイズ : 2
 第2引数の値 :10
 第2引数の値 :20
4回目  サイズ : 3
 第2引数の値 :10
 第2引数の値 :20
 第2引数の値 :30
```

# データベース
## データベースに接続したい
JDBCドライバをダウンロードすると、`java.sql.DriverManager`クラスに利用可能なドライバとして登録される。

<h3>実装例</h3>

```java:SqlConnect.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnect
{
    public static void main(String[] args)
    {
        try (Connection db = DriverManager.getConnection("データベースへのurl",
                                                         "root",
                                                         "password")){
            System.out.println("接続しました。");
        } catch (SQLException e) {
            System.out.println("接続エラー：" + e.getMessage());
        }
    }
}
```
# ファイル入出力
## ファイルを読み込みたい
`FileReader`オブジェクトを取得する。

```java
try {
    File file = new File(file_name);
    FileReader filereader = new FileReader(file);
} catch (FileNotFoundException e) {
    System.out.println(e);
}
```


# コレクション
## ArrayListの利用

```java:
import java.util.ArrayList;

public class Main
{
    public static void main(String[] args)
    {
        ArrayList<Integer> list = new ArrayList<>();
        Integer i1 = 10;
        int i2 = 2;
        Integer i3 = i1;
        list.add(i1);
        list.add(i2);
        list.add(i3);
        list.add(1, 5);
        System.out.println("size: " + list.size());
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i) + " ");
        }
        System.out.println();
        for (Integer i : list) { System.out.println(i + " "); }
    }
}
```


# 例外と例外処理
## 独自例外クラスの定義方法
Javaではプログラマが独自で例外クラスを定義することが可能。一般的にはExceptionクラスを継承して実装する。
```
[修飾子] class クラス名 extends Exception {

}
```

```java:example.java
private class MyException extends Exception
{

}
```

Exceptionクラスを継承することで、ExceptionクラスおよびそのスーパークラスであるThrowableクラスが
提供しているメソッドを引き継ぐことができる。
```
void printStackTrace()        エラーを追跡して発生箇所を特定するエラートレースを出力する
String getMessage()           エラーメッセージを取得する
```

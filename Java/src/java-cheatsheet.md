- [クラス・メソッド・プロパティ](#クラスメソッドプロパティ)
  - [仮引数を可変長にしたい](#仮引数を可変長にしたい)

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

# クラス・メソッド・プロパティ
## 仮引数を可変長にしたい
- データ型のあとに「...」と記述する。
- 可変長引数は最後に置く。
- 可変長引数は1つしか使用できない。
- 関数オーバーロードする場合、可変長引数を使って定義されたメソッドと引数リストがすべて明記されたメソッドでは後者のメソッドが優先して実行される。

### 実行例
```java:test1.java
class Test
{
    private void method(String s, int... a)
    {
        System.out.println(s + " サイズ : " + a.length);
        for (int i : a) {
            System.out.println(" 第2引数の値 :" + i)
        }
    }
}

private class Test1
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

# 抽象クラス
- 具体的な処理内容を持たないクラスで、継承される前提で用いられる。
- 一つでも抽象メソッドを持つクラスは、抽象クラスでなくてはならない。
- スーパークラスから受け継いだ抽象メソッドの具体的な処理内容をサブクラスで記述することも、メソッドのオーバーライドという。

## 抽象クラスの特徴
- 抽象メソッドを定義できる。
- オブジェクトを生成できず、継承されることで用いられることが前提。

```java:BankAccount.java
abstract class BankAccount {
    :
}

// 抽象クラスの継承の仕方は通常のサブクラスと同様にextendsを使用して継承する。
class SavingAccount extends BankAccount {
    :
}
```


## 抽象メソッド
- 抽象メソッドの定義は{}を書かず、「abstract void hello();」のように記述する。
- 修飾子としてprivate, static, finalは指定できない。
- サブクラスでオーバーライドして利用する。

```java:SavingAccount.java
public class SavingAccount extends BankAccount {
    :
    @Override
    public void display() {
        System.out.println("普通 「普通預金口座です」)");
        :
    }
    :
}
```

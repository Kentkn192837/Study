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


# インターフェース
- インターフェースの宣言ではメソッドの具体的な処理内容を記述せず、メソッドの型だけを先に定義する。 
- 複数のインターフェースを継承して新しいインターフェースを定義することができる。（多重継承）
- 具象メソッドとして、static, defaultメソッドを定義できる。
- defaultメソッドは実装クラスで利用したり、オーバーライドできる。
 
## インターフェースの宣言
```java:ATM.java
public interface ATM {
    :
    int NUMBER = 10;
    // public static final int NUMBER = 10; と同じ
    :
    void deposit(String accountName, int amount);
    // public abstract void deposit(String accountName, int amount); と同じ

    // privateを付与することもできる。
    static void staticMethod() {
        :
    }

    // 暗黙的に修飾子はpublicとなる。
    default void defaultMethod() {
        :
    }
}
```

- インターフェースで定義した定数は、修飾子を省略しても自動的に「public static final」が付与される。
- インターフェースで定義した抽象メソッドは、修飾子を省略しても自動的に「public abstract」が付与される。

## インターフェースの継承
```java:NewATM.java
interface NewATM extends ATM {
    :
}
```

- 既存のインターフェースを継承できる。
- スーパーインターフェースに定義されている定数やメソッドは、サブインターフェースに継承される。


## インターフェースの実装
```java:BankBranch.java
// [修飾子] class クラス名 implements インターフェース名 {}
public class BankBranch implements ATM {
    :
    @Override
    public void deposit(Srting accountName, int amount) {
        try {
            BankAccount account = findAccount(accountName);
            :
        }
    }
}
```

- インターフェースを実装したクラスはインスタンス化できる。
- クラスの継承とインターフェースの実装は同時にできる
```java
public class MyClass extends MySuperClass implements MyInterface {
    :
}
```

- 複数のインターフェースを実装する
```java
public class MyClass implements MyInterface1, MyInterface2 {
    :
}
```


- (インターフェース)クラスに含まれるメソッドの具体的な処理内容を記述せず、定数とメソッドの型のみを定義したもの
- (抽象クラス)抽象メソッドは具体的な処理内容を記述せず、メソッド名や引数などの定義だけを宣言する
<br>(参考)https://www.internetacademy.jp/it/programming/java/difference-between-interface-and-abstract.html?fbclid=IwAR2rBu0BPTYlaKCapSbiqEwC9qxuI7f6H3LcUCqqg4-2hVo6zmGsh3BH8BQ

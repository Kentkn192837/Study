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


# インタフェース
- インタフェースの宣言ではメソッドの具体的な処理内容を記述せず、メソッドの型だけを先に定義する。 
- 複数のインタフェースを継承して新しいインタフェースを定義することができる。（多重継承）
- 具象メソッドとして、static, defaultメソッドを定義できる。
- defaultメソッドは実装クラスで利用したり、オーバーライドできる。
 
## インタフェースの宣言
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

- インタフェースで定義した定数は、修飾子を省略しても自動的に「public static final」が付与される。
- インタフェースで定義した抽象メソッドは、修飾子を省略しても自動的に「public abstract」が付与される。

## インタフェースの継承
```java:NewATM.java
interface NewATM extends ATM {
    :
}
```

- 既存のインタフェースを継承できる。
- スーパーインタフェースに定義されている定数やメソッドは、サブインタフェースに継承される。


## インタフェースの実装
```java:BankBranch.java
// [修飾子] class クラス名 implements インタフェース名 {}
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

- インタフェースを実装したクラスはインスタンス化できる。
- クラスの継承とインタフェースの実装は同時にできる
```java
public class MyClass extends MySuperClass implements MyInterface {
    :
}
```

- 複数のインタフェースを実装する
```java
public class MyClass implements MyInterface1, MyInterface2 {
    :
}
```


- (インタフェース)クラスに含まれるメソッドの具体的な処理内容を記述せず、定数とメソッドの型のみを定義したもの
- (抽象クラス)抽象メソッドは具体的な処理内容を記述せず、メソッド名や引数などの定義だけを宣言する
<br>(参考)https://www.internetacademy.jp/it/programming/java/difference-between-interface-and-abstract.html?fbclid=IwAR2rBu0BPTYlaKCapSbiqEwC9qxuI7f6H3LcUCqqg4-2hVo6zmGsh3BH8BQ


# インタフェース実装例
```java:Program.java
import java.util.ArrayList;
interface Instrument {
    // public abstract void play(); と同じ
    void play();
}

class Piano implements Instrument {
    @Override
    public void play() {
        System.out.println("ポロンポロン");
    }
}

class Guitar implements Instrument {
    @Override
    public void play() {
        System.out.println("ジャカジャン");
    }
}

class Drum implements Instrument {
    @Override
    public void play() {
        System.out.println("ドドドン");
    }
}

class Program {
    public static void main(String[] args) {
        ArrayList<Instrument> instruments = new ArrayList<>();
        instruments.add(new Piano());
        instruments.add(new Guitar());
        instruments.add(new Drum());

        for (Instrument instrument: instruments) {
            instrument.play();
        }
    }
}
```

# ポリモフィズム
- 操作を利用する側が相手のオブジェクトのクラスを意識しなくても、相手のオブジェクトによって適切な処理が実行される仕組みのことをポリモフィズムという。
- ポリモフィズムではサブクラスのオブジェクトをすべてスーパークラスのオブジェクトとして扱うことができる。

### ポリモフィズム実装例
```java:
/*
    スーパークラス: BankAccount
    サブクラス: CheckingAccount, SavingsAccount, ForeignCurrencyAccount
*/
private ArrayList<BankAccount> accounts = new ArrayList<>();
accounts.add(new CheckingAccount(accountName, balance));
accounts.add(new SavingsAccount(accountName, balance));
accounts.add(new ForeignCurrencyAccount(accountName, balance));

for (BankAccount account: accounts) {
    account.display();
}
```


## 参照型のキャスト
クラス間に継承関係や実現関係がある場合、参照型でもキャストができる。
### キャストできる場合
- サブクラスのインスタンスをスーパークラスの型に代入する場合。
- インタフェースの実装クラスをインタフェースの型に代入する場合。

```java
// SavingsAccount型をBankAccount型にキャスト(暗黙的型変換)
BankAccount bankAccount = new SavingsAccount();

// BankAccount型をSavingsAccount型にダウンキャスト(明示的型変換)
SavingsAccount savingsAccount = (SavingsAccount)bankAccount;
```

### ダウンキャストできる場合
- スーパークラスの型で参照しているオブジェクトをサブクラスの型に代入する場合。(キャストしたオブジェクトを元の型に戻す)
- インタフェースの型で参照しているオブジェクトを実装クラスの型に代入する場合。(キャストしたオブジェクトを元の型に戻す)


## instanceof演算子
ダウンキャストを行う場合は、元のオブジェクトとの継承関係であったり、実現関係であったりしなければならない。これらに当てはまらない型同士をダウンキャストしようとすると、`java.lang.ClassCastException`の例外が発生する。

`java.lang.ClassCastException`が発生しないかどうか確認するために、`instanceof`演算子を利用する。
```java:example.java
public void accountDisplay(BankAccount account) {
    account.display();
    if (!account instanceof ForeignCurrencyAccount) {
        return;
    }
    // BankAccount型からForeignCurrencyAccount型にダウンキャスト
    ForeignCurrencyAccount foreignAccount = (ForeignCurrencyAccount)account;
    foreignAccount.currencyDisplay();
}
```

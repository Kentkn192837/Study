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


# Java API
## Objectクラス
`java.lang`パッケージで提供される`Object`クラスは、標準クラスとユーザー定義クラスすべてのクラスのスーパークラスとなる。したがって、暗黙的にすべてのクラスは`Object`クラスを継承している。
```java
// こう定義しても
class BankUser {
    :
}

// 暗黙的にこのように定義しているのと同じ
class BankUser extends Object {
    :
}
```

## == と equals() の仕組み
- `==`ではオブジェクト自身の参照先が同じかどうかを比較している。
- String型の`equals()`では文字列の情報をchar型を要素に持つ配列として属性に定義しているため、二つの配列から取り出した文字を1文字1文字比較して、すべての文字が同じなら`true`を返す処理を行っている。

```java:
String str1 = new String("Hello");
String str2 = new String("Hello");

// 参照先が違うのでfalseと表示される。
System.out.println(str1 == str2);
// equals()によって文字を一文字ずつ比較しているので、trueと表示される。
System.out.println(st1.equals(str2));
```

## コレクションAPI

## ジェネリクス
- クラスやメソッドの定義時に特定の型を指定せず、クラスやメソッドを利用する際に利用者側で型を指定するための機能。型パラメータには`T`,`E`,`K`,`V`のようなアルファベット1文字が使用されるが、任意の文字(列)を指定することもできる。

```java:
class MyClass<T> {
    private T prm;
}

MyClass<int> myClass = new MyClass<>();
```

# エラーへの対処
## 例外
- プログラム実行時のエラーを`例外`と言う。これに対し、スペルミスなどによってプログラムのコンパイル時に発生するエラーを`コンパイルエラー`と言う。
- 例外には`標準例外`と`ユーザー定義例外`がある。


## ユーザー定義例外
- 開発者が作成する例外で、`Exception`クラスのサブクラスとして定義し、必要なメンバ変数やメソッドを追加して作成する。

```java:exceptionexample.java
// Exceptionクラスを継承
class BusinessException extends Exception {
    BusinessException(String message) {
        super(message);     // エラーメッセージを継承する
    }
}
```

## 例外を明示的に発生させる方法
ユーザー定義例外を発生させるためには、`throw`を使用して例外を発生させるべき不適切な処理が行われた箇所で例外クラスのオブジェクトを生成してスローする。

```java:
void decreaseBalance(int amount) throws BusinessException {
    if (balance < amount) {
        throw new BusinessException("口座「預金残高が足りません」");
    }
    balance -= amount;
}
```

- `throws`キーワードはメソッド内にチェック例外を発生させる処理を記述した場合に使用する。非チェック例外の場合は省略できる。
- `throws`で発生する可能性のある例外を複数記述する時は`,`で区切る。

```java:
public int calcDiv(int a, int b) throws ArithmeticException, NullPointerException {
    :
}
```

## try-catch文の注意事項
### 複数の例外オブジェクトをキャッチする場合
catchブロックを複数記述する場合に指定する例外クラス間に継承関係がある場合は、サブクラスのcatchブロックをスーパークラスの例外より先に記述する。

```java:
try {
    :
} catch (NullPointerException e) {
    /*
        NullPointerException例外はException例外クラスのサブクラスなので、
        Exceptionクラスのcatchブロックより先に記述する。
    */
    :
} catch (Exception e) {
    :
} finally {
    // try-catchブロック内にreturn文があっても、finally内の処理は実行される。
    :
}
```

### multi-catch文を利用した例外処理
- 例外処理を`|`で分割すると、複数の種類の例外に対して、同じcatchブロックで例外処理を行う。
- 継承関係にある例外クラス同士はmulti-catch文で利用できない。

```java:
try {
    BankAccount account = findAccount(accountName);
    account.decreaseBalance(amount);
} catch(NullPointerException | BusinessException e) {
    // catch(NullPointerException | Exception e) とするとコンパイルエラーとなる
    System.out.println("例外が発生しました");
    e.getMessage();
    e.printStackTrace();
}
```

## 例外の再スロー
## try-with-resource文

# データ型
抜粋してまとめ。

## NUMBER型
|型の書式 |格納できるデータ |
|---|---|
|NUMBER(n)|n桁の整数。(n:1～38)|
|NUMBER(n, m)|全体でn桁, 小数点以下がm桁の小数または整数。(n:1～38)|
|NUMBER|NUMBER型で指定できる最大の精度|


# DDL
## SELECT
### DESCRIBEコマンド
### DUAL表
カラムが`DUMMY`列のみで、データを1行だけ持つ表。

```sql
SELECT * FROM DUAL;
DESC DUAL;
```

演算結果を確認するために使える。
```sql
SELECT 365 * 5 FROM DUAL;
SELECT ROUND(1.4) FROM DUAL;
```

### ORDER BYによるNULLの表示制御

### 置換変数
- 対話的指定: `&empno`
- 値を保持したい時は二重アンパサンドを使う: `&&empno`

二重アンパサンドを使うと、一度指定した置換変数の値への入力を再度求められなくなる。同じ値が使いまわされるようになる。

### DEFINEコマンド
`DEFINE`コマンドを使うと、SQL実行前にあらかじめ置換変数の値を設定しておくことができる。
```sql
DEFINE empno = 1001;
SET VERIFY OFF;
SELECT * FROM emp
  WHERE empno = &empno;
```

`VERIFY`というSQL*Plusシステム変数を使うと、置換変数をデフォルトで使用した時の「旧...」「新...」という表示を
ON, OFFで制御できる。

### NULLS FIRST, LASTによるNULLの表示位置のソート
`ORDER BY`句に`NULLS FIRST`または`NULLS LAST`を指定すると、NULLの表示位置を制御できる。
- `NULLS FIRST`でNULLを先頭に表示する。
- `NULLS LAST`でNULLを末尾に表示する。
```sql
SELECT
  ename, sal, comm_pct
FROM
  emp
ORDER BY
  comm_pct
DESC NULLS FIRST;
```

## SEQUENCE
### シーケンスから連番を取り出す
```sql
<シーケンス名>.NEXTVAL --シーケンスから連番を取り出す
<シーケンス名>.CURRVAL --振り出された連番を確認する
```

### シーケンスの一覧を取得
```sql
SELECT * FROM ALL_SEQUENCE; --ユーザーがアクセスできるシーケンスを取得する
SELECT * FROM DBA_SEQUENCE; --データベース内のシーケンスを取得する
SELECT * FROM USER_SEQUENCE; --ユーザーが所有するシーケンスを取得する
```
`LAST_NUMBER`が次に`NEXTVAL`を実行したときに採番される数値。

### シーケンスの書き換え
```sql
ALTER SEQUENCE <シーケンス名> RESTART START WITH 100; --指定したシーケンスを指定した数値から開始する。
```
## SYNONYM


# DROP TABLEによる表の削除
## ごみ箱からのフラッシュバックドロップ
Oracleには「ごみ箱」という機能があり、削除した表はごみ箱に入る。
ごみ箱から表を復元することをフラッシュバックドロップという。

```sql
FLASHBACK TABLE <表名> TO BEFORE DROP;
```

ごみ箱に入った削除済みの表は、単に表に対して「削除済み」というマークが付けられているだけで、
元々表が格納されていた表領域には存在する。


ごみ箱に移さずにテーブルを消すには、`PURGE`を使う。
```sql
DROP TABLE <表名> PURGE;
```

ドロップしたい表が外部キー制約で参照されている場合は、`CASCADE CONSTRAINTS`を使う必要がある。
```sql
DROP TABLE <表名> CASCADE CONSTRAINTS PURGE;
```

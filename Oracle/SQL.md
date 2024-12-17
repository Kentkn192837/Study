# データ型
抜粋してまとめ。

## NUMBER型
|型の書式 |格納できるデータ |
|---|---|
|NUMBER(n)|n桁の整数。(n:1～38)|
|NUMBER(n, m)|全体でn桁, 小数点以下がm桁の小数または整数。(n:1～38)|
|NUMBER|NUMBER型で指定できる最大の精度|


# DESCRIBEコマンド
# DUAL表
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

# ORDER BYによるNULLの表示制御

# 置換変数
- 対話的指定: `&empno`
- 値を保持したい時は二重アンパサンドを使う: `&&empno`

二重アンパサンドを使うと、一度指定した置換変数の値への入力を再度求められなくなる。同じ値が使いまわされるようになる。

# DEFINEコマンド
`DEFINE`コマンドを使うと、SQL実行前にあらかじめ置換変数の値を設定しておくことができる。
```sql
DEFINE empno = 1001;
SET VERIFY OFF;
SELECT * FROM emp
  WHERE empno = &empno;
```

`VERIFY`というSQL*Plusシステム変数を使うと、置換変数をデフォルトで使用した時の「旧...」「新...」という表示を
ON, OFFで制御できる。

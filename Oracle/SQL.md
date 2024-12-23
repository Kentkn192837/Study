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


# DML
## ユーザーに付与されている権限の確認
```sql
SELECT * FROM user_sys_privs; --データベースに接続しているユーザー自身に直接付与された権限
SELECT * FROM session_privs;
SELECT * FROM dba_sys_privs; --すべてのユーザーの情報を確認できるが、SELECT ANY TABLE権限を持ったユーザーであることが必要
SELECT * FROM role_sys_privs;
SELECT * FROM session_roles;
SELECT * FROM dba_role_privs;
SELECT * FROM user_tab_privs;
SELECT * FROM dba_data_files;
SELECT * FROM dba_free_space; --現行のユーザーがアクセスできる表領域内の使用可能な範囲
```


# Oracleでのデータバックアップ  Oracle Data Pump
Data Pumpを使用するには、ダンプファイルの入出力先となるOSのディレクトリに対応するディレクトリオブジェクトをあらかじめ作成しておく必要がある。

## ディレクトリオブジェクトの作成
```
CREATE [OR REPLACE] DIRECTORY <ディレクトリ名> AS '<対応するOSのディレクトリのパス>';
```

```sql
-- 実行例
CREATE OR REPLACE DIRECTORY dmp_dir AS '/u01/work/oracle_dmp_dir';
```

## 各モードでのエクスポート/インポート
```sql
--表モードでのエクスポート/インポート
expdp scott/tiger DUMPFILE=dmp_dir:expdat.dmp TABLS=emp,dept --接続ユーザーが所有する表のエクスポート
impdp scott/tiger DUMPFILE=dmp_dir:expdat.dmp TABLS=emp,dept --接続ユーザーが所有する表のインポート
expdp system/manager DUMPFILE=dmp_dir:expdat.dmp TABLS=scott.emp,blake.dept --他のユーザーが所有する表のエクスポート
impdp system/manager DUMPFILE=dmp_dir:expdat.dmp TABLS=scott.emp,blake.dept --他のユーザーが所有する表のインポート

--スキーマモードでのエクスポート/インポート
expdp scott/tiger SCHEMAS=scott DUMPFILE=dmp_dir:expdat.dmp --指定したスキーマが所有するすべてのオブジェクトをエクスポート
impdp scott/tiger SCHEMAS=scott DUMPFILE=dmp_dir:expdat.dmp --指定したスキーマが所有するすべてのオブジェクトをインポート

--表領域モードでのエクスポート/インポート
expdp scott/tiger TABLESPACE=tbs9 DUMPFILE=dmp_dir:dump_tbs9.dmp --指定した表領域に格納されているすべてのオブジェクトをエクスポート
impdp scott/tiger TABLESPACE=tbs9 DUMPFILE=dmp_dir:dump_tbs9.dmp --指定した表領域に格納されているすべてのオブジェクトをインポート

--全体モードのエクスポート/インポート
expdp scott/tiger FULL=y DUMPFILE=<ディレクトリ名>:<ファイル名> --データベース全体をエクスポートする。
impdp scott/tiger FULL=y DUMPFILE=<ディレクトリ名>:<ファイル名> --データベース全体をインポートする。

--トランスポート表領域モード
```

パラメーターファイルを用意して、expdp,impdpのパラメータを指定することもできる
```exp.par
SCHEMAS=user01
DIRECTORY=dmp_dir
DUMPFILE=expdat.dmp
LOGFILE=dmp.log
TABLE_EXISTS_ACTION=append
```

```
expdp scott/tiger PARFILE=exp.par
```

# 参考サイト
#### Oracle管理者のためのSQLリファレンス
- https://atmarkit.itmedia.co.jp/fdb/ref/ref_oracle.html

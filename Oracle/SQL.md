- [データ型](#データ型)
  - [NUMBER型](#number型)
  - [日時データ型](#日時データ型)
- [DDL](#ddl)
  - [SQLの実行順序](#sqlの実行順序)
  - [SELECT](#select)
    - [DESCRIBEコマンド](#describeコマンド)
    - [DUAL表](#dual表)
    - [ORDER BYによるNULLの表示制御](#order-byによるnullの表示制御)
    - [置換変数(`&`, `&&`)](#置換変数-)
    - [DEFINEコマンド](#defineコマンド)
    - [NULLS FIRST, LASTによるNULLの表示位置のソート](#nulls-first-lastによるnullの表示位置のソート)
- [Index](#index)
  - [SEQUENCE](#sequence)
    - [シーケンスから連番を取り出す](#シーケンスから連番を取り出す)
    - [シーケンスの一覧を取得](#シーケンスの一覧を取得)
    - [シーケンスの書き換え](#シーケンスの書き換え)
  - [SYNONYM](#synonym)
    - [シノニムの作成](#シノニムの作成)
    - [シノニムの削除](#シノニムの削除)
- [DROP TABLEによる表の削除](#drop-tableによる表の削除)
  - [ごみ箱からのフラッシュバックドロップ](#ごみ箱からのフラッシュバックドロップ)
- [DML](#dml)
  - [MERGE文](#merge文)
  - [ユーザーに付与されている権限の確認](#ユーザーに付与されている権限の確認)
- [Oracleでのデータバックアップ  Oracle Data Pump](#oracleでのデータバックアップ--oracle-data-pump)
  - [ディレクトリオブジェクトの作成](#ディレクトリオブジェクトの作成)
  - [ディレクトリオブジェクトの確認](#ディレクトリオブジェクトの確認)
  - [各モードでのエクスポート/インポート](#各モードでのエクスポートインポート)
- [RDS for Oracle DBインスタンスの管理](#rds-for-oracle-dbインスタンスの管理)
  - [Amazon S3 バケットからOracle DBインスタンスにファイルをダウンロードする](#amazon-s3-バケットからoracle-dbインスタンスにファイルをダウンロードする)
- [参考サイト](#参考サイト)
      - [Oracle管理者のためのSQLリファレンス](#oracle管理者のためのsqlリファレンス)
- [制約](#制約)
- [一時表](#一時表)
- [期間](#期間)
- [表領域](#表領域)
- [表領域の表示](#表領域の表示)
- [表領域の作成](#表領域の作成)
- [表領域の拡張](#表領域の拡張)
- [オプティマイザ統計の取得](#オプティマイザ統計の取得)


# データ型
抜粋してまとめ。

## NUMBER型
| 型の書式     | 格納できるデータ                                      |
| ------------ | ----------------------------------------------------- |
| NUMBER(n)    | n桁の整数。(n:1～38)                                  |
| NUMBER(n, m) | 全体でn桁, 小数点以下がm桁の小数または整数。(n:1～38) |
| NUMBER       | NUMBER型で指定できる最大の精度                        |


## 日時データ型
| 型の書式                                                                                                          | 格納できるデータ                                 |
| ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------ |
| DATE                                                                                                              | 日付と時刻（ミリ秒を含まない）                   |
| TIMESTAMP                                                                                                         | 日付と時刻（ミリ秒を含む）                       |
| TIMESTAMP WITH TIME ZONE                                                                                          | 日付と時刻（ミリ秒を含む）とタイムゾーン情報     |
| TIMESTAMP WITH LOCAL TIME ZONE                                                                                    | 日付と時刻（ミリ秒を含む）とタイムゾーン情報<br> |
| タイムゾーン情報は、UTCで保存されて、参照時にはデータベースに設定されているローカルタイムゾーン情報に変換される。 |


# DDL
## SQLの実行順序
`FROM`→`WHERE`→`GROUP BY`→`HAVING`→`SELECT`→`ORDER BY`
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

### 置換変数(`&`, `&&`)
- `&empno`が呼ばれる度に値の入力を求められる。: `&empno`
- `&&empno`が呼ばれるのは一度だけで、最初に入力した値が使いまわされる。: `&&empno`

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


# Index
Indexの一覧を表示
```sql
SELECT * FROM user_indexes;
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
シノニムを定義すると、オブジェクト（表, ビュー, シーケンス, プロシージャ, パッケージ）に別名を付けることができる。

シノニムにはプライベートシノニムとパブリックシノニムを定義することができる。

### シノニムの作成
```sql
CREATE OR REPLACE SYNONYM tbl0 FOR user1.tbl0; --他スキーマのオブジェクトにシノニムを定義できる。
CREATE OR REPLACE SYNONYM tbl1 FOR tbl1@DB1; --データベースリンクを経由してアクセスするリモートオブジェクトにもシノニムを定義できる。
```

### シノニムの削除
シノニムは`DROP SYNONYM`文で削除できる。
```
DROP [PUBLIC] SYNONYM <シノニム名>; --パブリックシノニムを削除したい場合は、PUBLICを付ける。
```


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
## MERGE文
指定した表から行を取得し、その行にもとづいて別の表の行を更新または追加できる。
```sql
MERGE 
INTO cust_copy c  -- 更新したい表
    USING ( 
        SELECT
            id
            , UPPER(name)
            , name
            , active 
        FROM
            customers
    ) p -- ここでSELECTしたデータで、更新したい表のデータをUPDATEまたはINSERTする。
        ON (c.id = p.id) WHEN MATCHED THEN UPDATE 
SET
    c.name = p.name
    , c.active = p.active WHEN NOT MATCHED THEN 
INSERT (c.id, c.name, c.active) -- データを更新するカラムを指定
VALUES (p.id, p.name, p.active);


MERGE 
INTO cust_copy c  -- 更新したい表
    USING ( -- DUAL表を駆使することで狙った値で書き換えることに使える。
        SELECT
            '0000A' AS id
            , 'Tom' AS name
            , '1' AS active 
        FROM
            DUAL
    ) p -- ここでSELECTしたデータで、更新したい表のデータをUPDATEまたはINSERTする。
        ON (c.id = p.id) WHEN MATCHED THEN UPDATE 
SET
    c.name = p.name
    , c.active = p.active WHEN NOT MATCHED THEN 
INSERT (c.id, c.name, c.active) -- データを更新するカラムを指定する（省略可）
VALUES (p.id, p.name, p.active);
```

## ユーザーに付与されている権限の確認
```sql
SELECT * FROM user_sys_privs; --データベースに接続しているユーザー自身に直接付与された権限
SELECT * FROM session_privs;
SELECT * FROM dba_sys_privs; --すべてのユーザーの情報を確認できるが、SELECT ANY TABLE権限を持ったユーザーであることが必要
SELECT * FROM role_sys_privs;
SELECT * FROM session_roles;
SELECT * FROM dba_role_privs;
SELECT * FROM user_tab_privs; --ユーザーが所有している権限の確認
SELECT * FROM dba_data_files;
SELECT * FROM dba_free_space; --現行のユーザーがアクセスできる表領域内の使用可能な範囲
```

```sql
-- 指定したスキーマへの権限付与の例
grant read, write on directory DATA_DIR to MY_SCHEMA_01;
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

## ディレクトリオブジェクトの確認
Oracleのディレクトリ状態を確認するには、`DBA_DIRECTORIES`を参照します。
```sql
SELECT * FROM DBA_DIRECTORIES;
```

## 各モードでのエクスポート/インポート
```bash
# 表モードでのエクスポート/インポート
expdp scott/tiger DUMPFILE=dmp_dir:expdat.dmp TABLS=emp,dept # 接続ユーザーが所有する表のエクスポート
impdp scott/tiger DUMPFILE=dmp_dir:expdat.dmp TABLS=emp,dept # 接続ユーザーが所有する表のインポート
expdp system/manager DUMPFILE=dmp_dir:expdat.dmp TABLS=scott.emp,blake.dept # 他のユーザーが所有する表のエクスポート
impdp system/manager DUMPFILE=dmp_dir:expdat.dmp TABLS=scott.emp,blake.dept # 他のユーザーが所有する表のインポート

# スキーマモードでのエクスポート/インポート
expdp scott/tiger SCHEMAS=scott DUMPFILE=dmp_dir:expdat.dmp # 指定したスキーマが所有するすべてのオブジェクトをエクスポート
impdp scott/tiger SCHEMAS=scott DUMPFILE=dmp_dir:expdat.dmp # 指定したスキーマが所有するすべてのオブジェクトをインポート

# 表領域モードでのエクスポート/インポート
expdp scott/tiger TABLESPACE=tbs9 DUMPFILE=dmp_dir:dump_tbs9.dmp # 指定した表領域に格納されているすべてのオブジェクトをエクスポート
impdp scott/tiger TABLESPACE=tbs9 DUMPFILE=dmp_dir:dump_tbs9.dmp # 指定した表領域に格納されているすべてのオブジェクトをインポート

# 全体モードのエクスポート/インポート
expdp scott/tiger FULL=y DUMPFILE=<ディレクトリ名>:<ファイル名> # データベース全体をエクスポートする。
impdp scott/tiger FULL=y DUMPFILE=<ディレクトリ名>:<ファイル名> # データベース全体をインポートする。

# トランスポータブル表領域モード
```

パラメーターファイルを用意して、expdp,impdpのパラメータを指定することもできる
```exp.par
--exp.par
SCHEMAS=user01
DIRECTORY=dmp_dir
DUMPFILE=expdat.dmp
LOGFILE=dmp.log
TABLE_EXISTS_ACTION=append
```

```bash
# 実行例
expdp scott/tiger PARFILE=exp.par
```

# SQL*Loader
外部表(`.csv`など)からOracleの表にデータをインポートするための機能。
外部表にはアクセスドライバとして、`ORACLE_LOADER`, `ORACLE_DATAPUMP`の2つがある。

- 使用する時は、`sqlldr`というコマンドを実行する。
- `control`パラメータに制御ファイルを指定して、どんなファイルからデータを読み取るのか、どのようにデータを扱うのかの定義を記述して使う。

```bash
sqlldr user1/pass1 control='sample.ctl'
```

```sample.ctl
LOAD DATA
```

# RDS for Oracle DBインスタンスの管理
RDS for Oracle DBインスタンスとAmazon S3バケット間でファイルを転送するには、Amazon RDSパッケージ`rdsadmin_s3_tasks`という
専用のプロシージャを使用する。

アップロード時に`GZIP`でファイルを圧縮し、ダウンロード時に解凍することができる。
- https://docs.aws.amazon.com/ja_jp/AmazonRDS/latest/UserGuide/oracle-s3-integration.using.html


## Amazon S3 バケットからOracle DBインスタンスにファイルをダウンロードする
`rdsadmin.rdsadmin_s3_tasks.download_from_s3`を使用する。
- https://xp-cloud.jp/blog/2017/05/11/1614/
- https://qiita.com/billtench/items/ac998ffe6e7887a4a8a7

# 参考サイト
#### Oracle管理者のためのSQLリファレンス
- https://atmarkit.itmedia.co.jp/fdb/ref/ref_oracle.html


# 制約
Oracleには、5種類の制約を設定できる。
| 制約                          | 説明                                                                                                     |
| ----------------------------- | -------------------------------------------------------------------------------------------------------- |
| NOT NULL制約                  | 列にNULLが入るのを禁止する。(=必ず値を指定することを強制する。)                                          |
| CHECK制約                     | 列に指定されたチェック条件を満たさない値が設定されることを禁止する。                                     |
| UNIQUE KEY制約(一意制約)      | 列、または列の組み合わせに重複値が入るのを禁止する。                                                     |
| PRIMARY KEY制約(主キー制約)   | 列、または列の組み合わせに重複値またはNULLが入るのを禁止する。<br>1つのテーブルに対して1つのみ設定可能。 |
| FOREIGN KEY制約(外部キー制約) | 列、または列の組み合わせの値が、関連する表の一意キーまたは主キーの値と一致することを保証する。           |

- `UNIQUE(重複はダメ)`、`PRIMARY KEY=(NOTNULL + UNIQUE)`と覚えればよい。
- 一意制約と主キー制約は作成されると自動的に索引(Index)が作成される。
- 参照される表（親表）、参照する表（子表）


# 一時表





# 期間


# 表領域
# 表領域の表示
表領域は、「データディクショナリビュー」と「動的パフォーマンスビュー」から確認できる。<br>
(`V$`と付くのが動的パフォーマンスビュー)

```sql
SELECT * FROM DBA_TABLESPACES;  --表領域の情報を確認できる
SELECT * FROM DBA_DATA_FILES;   --データファイルの情報を確認できる
SELECT * FROM DBA_FREE_SPACE;   --データファイルの空き領域の情報を確認できる
SELECT * FROM V$TABLESPACE;     --表領域の情報を確認できる
SELECT * FROM V$DATAFILE;       --データファイルの情報を確認できる
```

動的パフォーマンスビューはデータベースが`OPEN`状態でなくとも、データベースがMOUNTさえされていれば参照することができる。

# 表領域の作成
```sql
-- 2つのデータファイルで1つの永続表領域を作成する例
-- 表領域名: mytbs
-- DATAFILE: ファイルパス
-- SIZE: 初期サイズ
-- AUTOEXTEND: 自動拡張
CREATE TABLESPACE mytbs
  DATAFILE '/u01/app/oracle/mytbs01.dbf'
  SIZE 100M
  AUTOEXTEND OFF,
  '/u01/app/oracle/mytbs02.dbf'
  SIZE 200M
  AUTOEXTEND ON;
-- smallfile表領域は、複数のデータファイルで構成することができるが、
-- データファイル数が膨大になるデメリットがある。


-- UNDO表領域作成例
CREATE UNDO TABLESPACE undotbs2
  DATAFILE '/u01/app/oracle/undotbs2.dbf' SIZE 100M;

-- 一時表領域作成例
CREATE TEMPORARY TABLESPACE temp2
  TEMPFILE '/u01/app/oracle/temp2.dbf' SIZE 100M;

-- bigfile表領域
CREATE BIGFILE TABLESPACE bigtbs
  DATAFILE '/u01/app/oracle/bigtbs.dbf' SIZE 50G;
```

# 表領域の拡張
表領域は`データファイルをリサイズする`, `表領域にしいデータファイルを追加する`のいずれかの方法で拡張できる。

```sql
--データファイルのリサイズで表領域を拡張する場合
ALTER DATABASE
  DATAFILE '/u01/app/oracle/mytbs01.dbf' RESIZE 200M;

--データファイルの追加して表領域を拡張する場合
ALTER TABLESPACE mytbs
  ADD DATAFILE '/u01/app/oracle/mytbs01.dbf' SIZE 100M;
```


# オプティマイザ統計の取得
オプティマイザ統計は、手動もしくは自動で収集することができる。<br>

```sql
-- 手動で収集する場合のPL/SQLプロシージャ
EXECUTE DBMS_STATS.GATHER_DATABASE_STATS();
EXECUTE DBMS_STATS.GATHER_SCHEMA_STATS();
EXECUTE DBMS_STATS.GATHER_TABLE_STATS();
EXECUTE DBMS_STATS.GATHER_INDEX_STATS();
EXECUTE DBMS_STATS.GATHER_SYSTEM_STATS();
```

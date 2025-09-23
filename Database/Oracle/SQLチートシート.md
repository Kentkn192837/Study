# データディクショナリビュー
```sql
select * from user_tables;
select * from user_indexes;
select * from user_views;
select * from user_sequences;
select * from user_synonyms;
select * from user_constraints;
select * from user_users;
select * from user_tablespaces;
select * from dba_data_files;
select * from user_free_sapce;
select * from dictionary;
```

# NLS系設定値の確認
```sql:
show parameter NLS_DATE_LANGUAGE;
show parameter NLS_TIMESTAMP_TZ_FORMAT;
```

# 初期化パラメータの変換
```sql
alter session set NLS_DATE_FORMAT='RR/MM/DD HH24:MI:SS';
```

# 日付系の確認
```sql:
select CURRENT_DATE from dual;
select CURRENT_TIMESTAMP from dual;
select LOCALTIMESTAMP from dual;
select SYSDATE from dual;
select SYSTEMTIMESTAMP from dual;
select SESSIONTIMEZONE from dual;
select DBTIMEZONE from dual;
select * from tstz;
```

# 日付関連の変換ファンクション
```sql:
select TO_DATE('2021/01/01 01:00:00', 'YYYY/MM/DD HH24:MI:SS') + INTERVAL '1-2' YEAR TO MONTH from dual;
select TO_DATE('2021/01/01 01:00:00', 'YYYY/MM/DD HH24:MI:SS') + INTERVAL '4 5:12:10' DAY TO SECOND from dual;
select ADD_MONTH('2021/02/14 12:05:10', 1) from dual;
select MONTHS_BETWEEN('2021/03/14 00:00:00', '2021/02/14 00:00:00') from dual;
select MONTHS_BETWEEN('2021/03/14 00:00:00', '2021/02/13 00:00:00') from dual;
select NEXT_DAY('2021/01/02 10:11:12', 'Fri') from dual;
select LAST_DAY('2021/01/02 10:11:12') from dual;
select TO_CHAR(SYSDATE, 'YYYY/MM/DD HH24:MI:SS') from dual;
select TO_CHAR(CURRENT_DATE, 'YYYY/MM/DD HH24:MI:SS') from dual;
select TO_CHAR(SYSDATE, 'YYYY-MM-DD') from dual;
```

# NULL関連ファンクション
```sql:
-- inがNULL=replaceの値で置き換える
select NVL(in, replace) from dual;

-- in≠NULL→out1, in=NULL→out2
select NVL2(in, out1, out2) from dual;

-- in=check のとき、NULLを返す
select NULLIF(in, check) from dual;

-- in1～inNの中で、最初にNULLではない値が見つかったら、その値を返す
select COALESCE(in1, in2, in3, ..., inN) from dual;
```

# 複数列を比較対象にする副問い合わせ
```sql:
select
    empno
    , ename
    , job
    , sal 
from
    emp 
where
    (job, sal) = (select job, sal from emp where empno = 1001);
```

## これを応用してUPDATEすることもできる
```sql:
update emp 
set
    (sal, comm_pct) = ( 
        select
            sal
            , comm_pct 
        from
            emp 
        where
            empno = 1001
    ) 
where
    empno = 1006;
```

# SQL*Plusコマンド
```sql:
DEFINE
DESCRIBE
EXIT
SET
SHOW
SHOW PARAMETER
UNDEFINE
```

# CLEATE TABLE文
```sql:
/* 複数列に対する制約は、表レベルで指定する必要がある */
/* NOT NULL制約は列ごとにしか定義できない */
/* CONSTRAINT句は省略できる */
/* check制約を列レベルで指定する時は、指定した列以外を指定することはできない */
CREATE TABLE T_YEAR_MONTH (
    year NUMBER(4),
    month NUMBER(2),
    first_name VARCHAR2(10) CONSTRAINT first_name_notnull NOT NULL,
    last_name VARCHAR2(10) CONSTRAINT last_name_notnull NOT NULL,
    normal_price NUMBER(8),
    sale_price NUMBER(8),
    original_num VARCHAR2(2) CHECK(original_num >= 7),
    CONSTRAINT PK_YM PRIMARY KEY(year, month),
    CHECK(normal_price > sale_price)
);
```

# ALTER系統
```sql:
ALTER TABLE <表名> ADD <制約>; --制約を追加する。
ALTER TABLE <表名> ADD <列定義>; --表に列を追加する。
ALTER TABLE <表名> DISABLE CONSTRAINT <制約名> CASCADE; --制約を無効化する。CASCADEは依存関係がある制約も削除する
ALTER TABLE <表名> ENABLE CONSTRAINT <制約名>; --無効化した制約を有効化する。無効化した外部キー制約も一つ一つ有効化すること。
ALTER TABLE <表名> MODIFY <列名> [<データ型>] [DEFAULT <値>] [NULL | NOT NULL]; --列定義を変えられるのは全行がNULLの時だけ。ただし、DATE⇔TIMESTAMP, TIMESTAMP WITH LOCAL TIME ZONE型の変更はデータが入っていても列定義を変えることができる。
ALTER TABLE <表名> DROP COLUMN <列名> CASCADE CONSTRAINTS; --単一列の削除
ALTER TABLE <表名> DROP COLUMN (<列名1>, <列名2>, ...) CASCADE CONSTRAINTS; --複数列の削除
ALTER TABLE <表名> SET UNUSED COLUMN <列名> [CASCADE CONSTRAINTS] [ONLINE]; --列を未使用化する。未使用化した列は元に戻せない。
ALTER TABLE <表名> DROP UNUSED COLUMNS; --未使用化した列のストレージ領域の解放
ALTER TABLE <表名> READ ONLY; --表を読み取り専用にしてSELECT FOR UPDATEやTRUNCATE文を実行不可にする。（表の削除は実行できる）
ALTER TABLE <表名> READ WRITE; --読み書き可能な状態に戻す
ALTER TABLE <表名> MOVE TABLESPACE <表領域名>; --表を別の表領域に異動する。元のテーブルに設定されていた索引はUNUSABLEになる。
ALTER TABLE <現在の表名> RENAME TO <新しい表名>; --テーブル名を新しい表名に変更する。
ALTER INDEX <索引名> UNUSABLE; --使用不可: 索引セグメントが削除されるので、索引データ自体が失われる。
ALTER INDEX <索引名> INVISIBLE; --不可視化: 索引の実態は残る。索引の有無による影響調査に使える。
```

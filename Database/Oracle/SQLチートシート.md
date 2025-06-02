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

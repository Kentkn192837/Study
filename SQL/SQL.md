# 達人に学ぶSQL徹底指南書 知見
- [達人に学ぶSQL徹底指南書 知見](#達人に学ぶsql徹底指南書-知見)
  - [SQLを高速化する手法](#sqlを高速化する手法)
    - [サブクエリ(副問い合わせ)を引数に取る場合は、INよりもEXISTSを使う](#サブクエリ副問い合わせを引数に取る場合はinよりもexistsを使う)
    - [サブクエリ(副問い合わせ)を引数に取る場合は、INよりも結合を使う](#サブクエリ副問い合わせを引数に取る場合はinよりも結合を使う)
    - [集合演算子のALLオプションを使う](#集合演算子のallオプションを使う)
    - [DISTINCTをEXISTSで代用する](#distinctをexistsで代用する)
    - [インデックスを利用する時は、列は裸](#インデックスを利用する時は列は裸)
    - [中間テーブルを減らすように工夫する](#中間テーブルを減らすように工夫する)
  - [SQLの実行順序](#sqlの実行順序)

## SQLを高速化する手法
### サブクエリ(副問い合わせ)を引数に取る場合は、INよりもEXISTSを使う
### サブクエリ(副問い合わせ)を引数に取る場合は、INよりも結合を使う
### 集合演算子のALLオプションを使う
### DISTINCTをEXISTSで代用する
### インデックスを利用する時は、列は裸
### 中間テーブルを減らすように工夫する
副問い合わせで中間テーブルを作成してデータを取得するようなSQLは、集約関数+`HAVING`句を使って、
中間テーブルを使わないように工夫することを考えること。<br>
集約した結果に対する条件は、`HAVING`句を使うのが鉄則。
```sql
-- before
SELECT
    * 
FROM
    ( 
        SELECT
            sale_data
            , MAX(quantity) AS max_qty 
        FROM
            SalesHistory 
        GROUP BY
            sale_date
    ) TMP -- 無駄な中間テーブル
WHERE
    max_qty >= 10;

-- after
SELECT
    sale_data
    , MAX(quantity) 
FROM
    SalesHistory 
GROUP BY
    sale_date 
HAVING
    MAX(quantity) >= 10;
```

## SQLの実行順序
`FROM`→`WHERE`→`GROUP BY`→`HAVING`→`SELECT`→`ORDER BY`

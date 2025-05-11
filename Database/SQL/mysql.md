# データベースの作成
```sql:
CREATE DATABASE データベース名;
```

# データベースの一覧を確認する
```sql:
SHOW DATABASES;
```

# 利用するデータベースの指定
```sql:
use データベース名;
```

### 現在利用しているデータベースの確認
```sql:
SELECT DATABASE();
```

# テーブルの作成
初めて作成したデータベースはまだ空っぽの状態である。テーブルを作成することでデータを挿入することができる。

| カラム名 | id | name |age|
|:-------|:----------:|:----------:|:---:|
| データ型| VARCHAR(10) | VARCHAR(10) | INT |

以上の内容のテーブルを作成するとすると、

```sql:
CREATE TABLE テーブル名(id VARCHAR(10), name VARCHAR(10), age INT);
```

さらに、エンコーディングを設定する場合は、次のようにする。
```sql:
CREATE TABLE テーブル名(id VARCHAR(10), name VARCHAR(10), age INT)
CHARSET=utf8;
```

### すべてのテーブルの表示
```sql:
SHOW TABLES;
```

useでデータベースを選択した状態で、他のデータベースにあるテーブルに対して操作したい場合は次のようにする。

```sql:
SELECT * FROM 他のデータベース名.テーブル名;
```

# テーブルのカラム構造の確認
```sql:
DESC テーブル名;
```

# テーブルにデータを挿入する
```sql:
INSERT INTO テーブル名 VALUES('データ', データ);
```

- カラム名を指定する場合
```sql:
INSERT INTO テーブル名 (カラム1, カラム2) VALUES(データ1, データ2);
```

- 複数のデータを一度に挿入する場合
,を付けてその先に続けて入力する。
```sql:
INSERT INTO テーブル名 (カラム1, カラム2) VALUES(データ1, データ2),
(データ1, データ2), (データ1, データ2);
```

# データの表示
先に示したテーブルを例にすると、idとnameの列のデータを表示するSQLは次のようになる。
```sql:
SELECT id, name FROM テーブル名;
```

- すべてのカラムを表示する場合
```sql:
SELECT * FROM テーブル名;
```

- SELECTコマンドはデータベースと関係ない値を表示させることもできる。
```sql:
SELECT 'テスト';
```

```:出力
+----------+
|  テスト  |
+----------+
|  テスト  |
+----------+
```

```sql:
SELECT (2 + 3) * 4;
```

```:出力
20
```

# テーブルのコピー
## テーブルのカラム構造とレコードの両方のコピー
```sql:
CREATE TABLE 新しいテーブル名 SELECT * FROM コピー元のテーブル名;
```

## テーブルのカラム名のみコピー
```sql:
CREATE TABLE 新しいテーブル名 LIKE 元となるテーブル名;
```

## 他のテーブルのレコードをコピー
同じカラム構造のテーブルのデータを丸ごとコピーする際に利用する。
```sql:
INSERT INTO テーブル名 SELECT * FROM 元となるテーブル名;
```

# データ型


# SQLサンプルコード
## 基本の検索
1. `SALES`表から全ての列を取得する。
```sql:
select * from SALES;
```

2. `NEW_PRODUCTS`表から全ての列を`PRODUCT_ID`が昇順になるように取得する。
```sql:
select * from NEW_PRODUCTS order by PRODUCT_ID asc;
```

3. `PRODUCTS2`表から`PRODUCT_NAME`,`LIST_PRICE`列を取得する。
```sql:
select * from PRODUCTS2;
```

4.  
```sql:
select PRODUCT_ID, SALES_DATE, PRICE * NUM from SALES;
```

5. 
```sql:
select * from CATEGORIES where CATEGORY_ID <> 'KE';
```

6. 
```sql:
select * from STAFFS where STAFF_NAME = '田中賢介';
```

7. 
```sql:
select * from STOCK where STOCK_NUM >= 5;
```

8. `SALES`表から`CLIENTELE_ID`が`03`または`08`である行を選択して全ての列を取得する。
```sql:
select * from SALES where CLIENTELE_ID in('03', '08');
```

9. `STAFFS`表から`STORE_ID`が`002`または`003`でない行を選択して全ての列を取得する。
```sql:
select * from STAFFS where STORE_ID not in('002', '003');
```

10. 
```sql:
select * from NEW_PRODUCTS
where LIST_PRICE between 10000 and 15000;
```

11. 
```sql:
select * from SALES
where PRICE * NUM not between 50000 and 100000;
```

12. `PRODUCT2`表から`PRODUCT_NAME`が`%黒%`であるような列を全て取得する。
LIKE演算子
- 「%」は、任意の文字数からなる文字列
- 「_」は任意の一文字
```sql:
select * from PRODUCT2
where PRODUCT_NAME like '%黒%';
```

13. 
```sql:
select * from STAFFS
where STAFF_NAME like '_田%';
```

14. 
```sql:
select * from STAFFS
where MAN_STAFF_ID is NULL;
```

15. 
```sql:
select * from STAFFS
where MAN_SATFF_ID is not NULL;
```

16. `PRODUCTS2`表から`LIST_PRICE`が昇順になるように全ての列を取得する。
```sql:
select * from PRODUCTS2 order by LIST_PRICE asc;
```

17. 
```sql:
select CLIENTELE_ID, SUM(PRICE * NUM) from SALES group by CLIENTELE_ID;
```

18. 
```sql:
select SUPP_ID, AVG(COST_PRICE) from PRODUCTS2 group by SUPP_ID;
```

19. 
```sql:
select STAFF_ID, COUNT(*) from SALES
where STAFF_ID = '010161' group by STAFF_ID;
```

20. 
```sql:
select SALES_DATE, PRODUCT_ID, PRICE * NUM from SALES
order by SALES_DATE, PRICE * NUM desc;
```

21. 
having句は集合関数(COUNT, SUM, AVG, MAX, MIN)で処理した結果を検索条件に指定する場合に用いる。
```sql:
select STAFF_ID, SUM(PRICE * NUM) from SALES
group by STAFF_ID having SUM(PRICE * NUM) >= 80000;
```

22. 
```sql:
select CLIENTELE_ID, AVG(PRICE * NUM) from SALES
where CLIENTELE_ID between '01' and '05'
group by CLIENTELE_ID having AVG(PRICE * NUM) >= 20000;
```

23. 
```sql:
select STAFF_ID, SUM(PRICE * NUM) from SALES
where STAFF_ID in ('020122', '030114', '010144')
group by STAFF_ID having SUM(PRICE * NUM) >= 90000;
```

## 応用の検索
1. 
内部結合`inner join`句を使用した場合、結合条件に`on`句を利用する。
```sql:
select STORES.STORE_ID, STORES.STORE_NAME, LOCATIONS.LOC_ID, LOC_NAME
from STORES inner join LOCATIONS
on STORES.LOC_ID = LOCATIONS.LOC_ID;
```

2. 
```sql:
select PRODUCTS2.PRODUCT_NAME, PRODUCTS2.LIST_PRICE, SUPPLIERS.SUPP_ID, SUPP_NAME
from PRODUCTS2 inner join SUPPLIERS
on PRODUCTS2.SUPP_ID = SUPPLIERS.SUPP_ID;
```

3. 
```sql:
select STAFFS.STAFF_ID, STAFFS.STAFF_NAME, PRODUCT_ID, SALES_DATE
from STAFFS inner join SALES
on STAFFS.STAFF_ID = SALES.STAFF_ID;
```

4.  
```sql:
select
    STAFF_ID, STAFF_NAME, STORES.STORE_ID, STORE_NAME, ADDRESS
from
    STAFFS inner join STORES
on
    STAFFS.STAFF_ID = STORES.STORE_ID;
```

5. 
```sql:
select
    COLOR_ID, COLOR_NAME, SIZE_ID, SIZE_NAME
from
    COLORS cross join SIZES;
```

6. 
```sql:
select
    PRODUCTS2.PRODUCT_ID, NUM, PRODUCT_NAME
from
    PRODUCTS2 left outer join SALES
on
    SALES.PRODUCT_ID = PRODUCTS2.PRODUCT_ID;
```

7. 
```sql:
select
    PRODUCTS2.PRODUCT_ID, PRODUCT_NAME, STOCK_NUM
from
    STOCK right outer join PRODUCTS2
on
    STOCK.PRODUCT_ID = PRODUCTS2.PRODUCT_ID;
```

8. 
```sql:
select
    WORKER.STAFF_ID, WORKER.STAFF_NAME, MANAGER.STAFF_NAME as MANAGER_NAME
from
    STAFFS as WORKER inner join STAFFS as MANAGER
on
    WORKER.MAN_STAFF_ID = MANAGER.STAFF_ID;
```

9. 
```sql:
select
    WORKER.STAFF_ID, WORKER.STAFF_NAME, MANAGER.STAFF_NAME as MANAGER_NAME
from
    STAFFS as WORKER left outer join STAFFS as MANAGER
on
    WORKER.MAN_STAFF_ID = MANAGER.STAFF_ID;
```

10. 
副問い合わせに関するSQL
```sql:
select
    STAFF_ID, PRODUCT_ID, PRICE * NUM as SUM_PRICE, SALES_DATE
from
    SALES
where
    STAFF_ID = (
        select STAFF_ID from STAFFS where STAFF_NAME = '田中正義';
    );
```

11.  
```sql:
select
    PRODUCT_ID, PRODUCT_NAME, SUPP_ID
from
    NEW_PRODUCTS
where
    SUPP_ID in (
        select SUPP_ID from SUPPLIERS
            where SUPP_NAME in('イクミジャパン株式会社', 'クガガール商事株式会社')
    );
```

12.  
```sql:
select
    PRODUCT_ID, PRICE * NUM as SUM_PRICE
from
    SALES
where
    PRICE * NUM > (
        select AVG(PRICE * NUM) from SALES
    );
```

13.  
```sql:
select
    STAFF_ID, STAFF_NAME, STORES.STORE_ID, STORE_NAME, ADDRESS, LOCATIONS.LOC_ID, LOC_NAME
from
    STAFFS inner join STORES
on
    STAFFS.STORE_ID = STORES.STORE_ID inner join LOCATIONS
on
    STORES.LOC_ID = LOCATIONS.LOC_ID;
```

14.  
```sql:
select
    STAFFS.STAFF_ID, STAFF_NAME, PRODUCT_ID, SALES_DATE, STORES.STORE_ID, STORE_NAME, ADDRESS
from
    STAFFS inner join SALES
on
    STAFFS.STAFF_ID = SALES.STAFF_ID inner join STORES
on
    STAFFS.STORE_ID = STORES.STORE_ID;
```

15.  
```sql:
select
    STORES.STORE_ID, STORE_NAME, SUM(PRICE * NUM)
from
    STAFFS inner join SALES
on
    STAFFS.STAFF_ID = STORES.STAFF_ID inner join STORES
on
    STAFFS.STORE_ID = STORE.STORE_ID
group by
    STORES.STORE_ID, STORE_NAME
```

16.  
```sql:
select
    STORES.STORE_ID, STORE_NAME, SUM(PRICE * NUM) as ALL_SUM_PRICE
from
    STAFFS inner join SALES
on
    STAFFS.STAFF_ID = SALES.STAFF_ID inner join STORES
on
    STAFFS.STORE_ID = STORES.STORE_ID
group by
    STORES.STORE_ID, STORE_NAME
having
    SUM(PRICE * NUM) > 300000;
```

17.  
```sql:
select
    PRODUCT_ID, PRODUCT_NAME
from
    PRODUCTS2
where
    PRODUCT_ID in (
        select PRODUCT_ID from STOCK where STOCK_NUM = (
            select MAX(STOCK_NUM) from STOCK
        )
    );
```

## 
1. 
```sql:
```

2. 
```sql:
```

3. 
```sql:
```

4.  
```sql:
```

5. 
```sql:
```

6. 
```sql:
```

7. 
```sql:
```

8. 
```sql:
```

9. 
```sql:
```

10.  
```sql:
```

11.  
```sql:
```

12.  
```sql:
```

13.  
```sql:
```

14.  
```sql:
```

15.  
```sql:
```

16.  
```sql:
```

17.  
```sql:
```

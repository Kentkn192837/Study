# 表領域
# 表領域の表示
表領域は、「データディクショナリビュー」と「動的パフォーマンスビュー」から確認できる。

| ビュー名          | 説明                                                                                     |
|-------------------|------------------------------------------------------------------------------------------|
| DBA_TABLESPACES   | データベースに存在する表領域の一覧のビュー                                              |
| DBA_DATA_FILES    | 表領域を構成するデータファイルの一覧のビュー                                            |
| DBA_FREE_SPACE    | データファイルの空き領域に関する情報のビュー                                            |
| V$TABLESPACE      | データベースに存在する表領域の一覧のビュー。データベースがOPENでなくても、MOUNTされていれば結果を参照できる。 |
| V$DATAFILE        | 表領域を構成するデータファイルの一覧のビュー。データベースがOPENでなくても、MOUNTされていれば結果を参照できる。 |



```sql
SELECT * FROM DBA_TABLESPACES;
SELECT * FROM DBA_DATA_FILES;
SELECT * FROM DBA_FREE_SPACE;
SELECT * FROM V$TABLESPACE;
SELECT * FROM V$DATAFILE;
```

# 表領域の作成
## 永続表領域
## UNDO表領域
## 一時表領域

# 表領域の拡張

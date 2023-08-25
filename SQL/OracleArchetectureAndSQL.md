# Oracleデータベースのアーキテクチャ
## Databaseサーバのアーキテクチャ
データベースサーバは、インスタンスとデータベースで構成されている。

### インスタンス
- データベースサーバでのインスタンスは、SGA(System Global Area)とバックグラウンドプロセス群で構成される。
- インスタンスは、環境変数`ORACLE_SID`により識別される。

```:インスタンス起動例
SQL> CONNECT /AS SYSDBA
```

#### メモリの構成情報の出力
※`v$...`で始まるビューは「動的パフォーマンスビュー」という。
```
SQL> SELECT name, value FROM v$sga;
```

#### プロセス出力例
```
SQL> SELECT name FROM v$bgprocess;
```

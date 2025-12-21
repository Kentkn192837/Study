- [知見](#知見)
  - [DESCはDESCRIBEコマンドの省略形](#descはdescribeコマンドの省略形)
  - [`''`で囲った文字列の内部で、単なる文字列としてシングルクォートを使いたい場合の対処](#で囲った文字列の内部で単なる文字列としてシングルクォートを使いたい場合の対処)
  - [列の別名に`$`, `_`, `#`以外の記号を含むとき（` `, `-`など）は、文字列を`""`で囲う](#列の別名に-_-以外の記号を含むとき---などは文字列をで囲う)
- [要注意！落とし穴リスト](#要注意落とし穴リスト)
  - [4章 リスナー, サービス登録](#4章-リスナー-サービス登録)
    - [リスナーの動的サービス登録するには、LOCAL\_LISTENER初期化パラメータを設定すること](#リスナーの動的サービス登録するにはlocal_listener初期化パラメータを設定すること)
    - [動的サービス登録で、デフォルト値でリスナーを起動させるならlistener.oraは不要](#動的サービス登録でデフォルト値でリスナーを起動させるならlisteneroraは不要)
    - [リスナーの静的サービス登録するには、listener.oraのSID\_LIST\_\<リスナー名\>パラメータにデータベース情報を記述しておくこと。](#リスナーの静的サービス登録するにはlisteneroraのsid_list_リスナー名パラメータにデータベース情報を記述しておくこと)
    - [Oracle Database Gatewayを使って外部の別のDBMS製品とデータ連携できる（異機種間サービス）](#oracle-database-gatewayを使って外部の別のdbms製品とデータ連携できる異機種間サービス)
  - [5章 権限の管理](#5章-権限の管理)
    - [「セグメント作成の遅延」が無効→クオータ（表領域の割り当て制限）が割り当てられていないと、表や索引などセグメントを伴うオブジェクトを作成することができない](#セグメント作成の遅延が無効クオータ表領域の割り当て制限が割り当てられていないと表や索引などセグメントを伴うオブジェクトを作成することができない)
    - [「セグメント作成の遅延」が有効→クオータが割り当てられていなくても、表や索引などセグメントを伴うオブジェクトを作成することができる。](#セグメント作成の遅延が有効クオータが割り当てられていなくても表や索引などセグメントを伴うオブジェクトを作成することができる)
    - [システム権限の付与→WITH ADMIN OPTION, オブジェクト権限の付与→WITH GRANT OPTION](#システム権限の付与with-admin-option-オブジェクト権限の付与with-grant-option)
    - [権限を取り消すときは、一旦権限付与を取り消してから、WITH ADMIN OPTIONなしで再度権限を付与する。](#権限を取り消すときは一旦権限付与を取り消してからwith-admin-optionなしで再度権限を付与する)
    - [PUBLICロールに権限付与すると、すべてのOracleユーザでその権限が有効になる。](#publicロールに権限付与するとすべてのoracleユーザでその権限が有効になる)
    - [SET ROLE文で、そのセッションにおけるロールの有効/無効を切り替えることができる。](#set-role文でそのセッションにおけるロールの有効無効を切り替えることができる)
    - [権限分析(DBMS\_PRIVILEGE\_CAPTURE)パッケージで権限分析対象にできるのは、「データベース全体」「ロール」「コンテキスト」「ロール＋コンテキスト」の4パターン](#権限分析dbms_privilege_captureパッケージで権限分析対象にできるのはデータベース全体ロールコンテキストロールコンテキストの4パターン)
    - [管理権限を持つユーザー⇒パスワードファイル認証, それ以外の一般ユーザー⇒データディクショナリでログイン情報が管理されている。](#管理権限を持つユーザーパスワードファイル認証-それ以外の一般ユーザーデータディクショナリでログイン情報が管理されている)
    - [一般ユーザでもOS認証に切り替えることができるが、Oracleユーザ名にOS\_AUTHENT\_PREFIX初期化パラメータで指定した接頭辞を付けたものを使用してCREATE USER文にIDENTIFIED EXTERNALLY句を付けて実行する。](#一般ユーザでもos認証に切り替えることができるがoracleユーザ名にos_authent_prefix初期化パラメータで指定した接頭辞を付けたものを使用してcreate-user文にidentified-externally句を付けて実行する)
    - [UNLIMITED TABLESPACEシステム権限だけは特別で、ロールに付与することができない。](#unlimited-tablespaceシステム権限だけは特別でロールに付与することができない)
  - [6章 表領域の概念](#6章-表領域の概念)
    - [SYSTEM表領域が使用できなくなった場合、インスタンスは異常終了する](#system表領域が使用できなくなった場合インスタンスは異常終了する)
    - [データベースデフォルトの表領域のブロックサイズは変更できない](#データベースデフォルトの表領域のブロックサイズは変更できない)
    - [Oracleのエクステント管理方式は、ほとんどの場合「ローカル管理方式」が採用されている](#oracleのエクステント管理方式はほとんどの場合ローカル管理方式が採用されている)
    - [エクステントの管理方式 ローカル管理方式⇒表領域の管理用ブロックでエクステントを管理している(高速), ディクショナリ管理方式⇒データディクショナリ(SYSTEM表領域)でエクステントを管理している。(低速)](#エクステントの管理方式-ローカル管理方式表領域の管理用ブロックでエクステントを管理している高速-ディクショナリ管理方式データディクショナリsystem表領域でエクステントを管理している低速)
    - [セグメント割り当てタイプは、UNDO表領域⇒AUTOALLOCATE, 一時表領域⇒UNIFORMで固定](#セグメント割り当てタイプはundo表領域autoallocate-一時表領域uniformで固定)
    - [bigfile表領域のデータファイル数は1で固定](#bigfile表領域のデータファイル数は1で固定)
    - [DROP TABLESPACE mytbs; (空の表領域を削除できる)(アクティブなトランザクションがあると、表領域は削除できない)](#drop-tablespace-mytbs-空の表領域を削除できるアクティブなトランザクションがあると表領域は削除できない)
    - [DROP TABLESPACE mytbs INCLUDING CONTENTS; (表領域と一緒にオブジェクトも削除する(OMF管理だとデータファイルも消える))](#drop-tablespace-mytbs-including-contents-表領域と一緒にオブジェクトも削除するomf管理だとデータファイルも消える)
    - [DROP TABLESPACE mytbs INCLUDING CONTENTS AND DATAFILES; (表領域と一緒にオブジェクトとデータファイルを削除する)](#drop-tablespace-mytbs-including-contents-and-datafiles-表領域と一緒にオブジェクトとデータファイルを削除する)
    - [OMF管理とは、データベースファイル管理をOracleで自動化する機能で、データファイル名が自動で決まる。不要なデータファイルは削除される。](#omf管理とはデータベースファイル管理をoracleで自動化する機能でデータファイル名が自動で決まる不要なデータファイルは削除される)
    - [DB\_CREATE\_FILE\_DEST, DB\_CREATE\_ONLINE\_LOG\_DEST\_n, DB\_RECOVERY\_FILE\_DEST ⇒OMF管理に関する問題ですよ。ということ。](#db_create_file_dest-db_create_online_log_dest_n-db_recovery_file_dest-omf管理に関する問題ですよということ)
  - [7章 表領域](#7章-表領域)
    - [ローカル管理方式でできること⇒エクステント割り当ての自動化](#ローカル管理方式でできることエクステント割り当ての自動化)
    - [自動セグメント領域管理⇒SEGMENT MANAGEMENT AUTO句を指定すると自動セグメント領域管理方式が使用できる。](#自動セグメント領域管理segment-management-auto句を指定すると自動セグメント領域管理方式が使用できる)
    - [再開可能文4選⇒(1)一時表領域を使うSELECT文,(2)DML,(3)ロード系処理(Data Pump,SQL\*Loader),(4)DDL:create table as select等](#再開可能文4選1一時表領域を使うselect文2dml3ロード系処理data-pumpsqlloader4ddlcreate-table-as-select等)
    - [再開可能領域割当てが動作する状況](#再開可能領域割当てが動作する状況)
    - [再開可能領域割り当ての機能を使うためには⇒RESUMABLE\_TIMEOUT初期化パラメータを設定する](#再開可能領域割り当ての機能を使うためにはresumable_timeout初期化パラメータを設定する)
    - [ALTER TABLE SHRINK SPACE（セグメント縮小）するためには、行移動を有効化(ENABLE ROW MOVEMENT)すること。](#alter-table-shrink-spaceセグメント縮小するためには行移動を有効化enable-row-movementすること)
    - [ALTER TABLE SHRINK SPACE文でセグメントを縮小して、HWMの位置を変更できる。](#alter-table-shrink-space文でセグメントを縮小してhwmの位置を変更できる)
    - [ALTER TABLE emp SHRINK SPACE; ⇒セグメント内データの再編成（片側に寄せる処理）セグメントの縮小+HWMの引き下げ](#alter-table-emp-shrink-space-セグメント内データの再編成片側に寄せる処理セグメントの縮小hwmの引き下げ)
    - [ALTER TABLE emp SHRINK SPACE COMPACT; ⇒片側に寄せる処理+HWMの引き下げ⇒COMPACTは、片側に寄せる処理コンパクトにする処理だけを実行するということ。](#alter-table-emp-shrink-space-compact-片側に寄せる処理hwmの引き下げcompactは片側に寄せる処理コンパクトにする処理だけを実行するということ)
    - [ALTER TABLE emp SHRINK SPACE CASCADE; ⇒片側に寄せる処理+セグメントの縮小+HWMの引き下げ+索引にも縮小処理をする。](#alter-table-emp-shrink-space-cascade-片側に寄せる処理セグメントの縮小hwmの引き下げ索引にも縮小処理をする)
    - [ALTER INDEX ... SHRINK SPACE \[...\]; ⇒を使えば、索引だけピンポイントで縮小処理をすることもできる。](#alter-index--shrink-space--を使えば索引だけピンポイントで縮小処理をすることもできる)
    - [TRUNCATE TABLEを実行すると、全行削除するだけではなく、エクステントを解放するかどうかについても制御できる。](#truncate-tableを実行すると全行削除するだけではなくエクステントを解放するかどうかについても制御できる)
    - [TRUNCATE TABLE tbl01; --デフォルトではDROP STORAGEと同じ](#truncate-table-tbl01---デフォルトではdrop-storageと同じ)
    - [TRUNCATE TABLE tbl01 DROP STORAGE; 表の作成後に追加で作成されたエクステントを解放する。](#truncate-table-tbl01-drop-storage-表の作成後に追加で作成されたエクステントを解放する)
    - [TRUNCATE TABLE tbl01 DROP ALL STORAGE; すべてのエクステントを解放する。](#truncate-table-tbl01-drop-all-storage-すべてのエクステントを解放する)
    - [TRUNCATE TABLE tbl01 REUSE STORAGE; エクステントを解放せずに使い続ける。](#truncate-table-tbl01-reuse-storage-エクステントを解放せずに使い続ける)
    - [セグメント縮小⇒ALTER TABLE ... SHRINK SPACE \[COMPACT\] \[CASCADE\]](#セグメント縮小alter-table--shrink-space-compact-cascade)
    - [TRUNCATE TABLE ... ⇒エクステントの解放](#truncate-table--エクステントの解放)
    - [表圧縮⇒ROW STORE COMPRESS BASIC, ROW STORE COMPRESS, COMPRESS, ROW STORE COMPRESS ADVANCEDのこと。](#表圧縮row-store-compress-basic-row-store-compress-compress-row-store-compress-advancedのこと)
  - [8章 UNDO表領域](#8章-undo表領域)
    - [UNDO⇒過去データが必要な処理（読取り一貫性の保証, ロールバック, 多重化できない, フラッシュバック機能）](#undo過去データが必要な処理読取り一貫性の保証-ロールバック-多重化できない-フラッシュバック機能)
    - [REDO⇒障害からの復旧時の再実行で使われる（ロールフォワード, 多重化できる）](#redo障害からの復旧時の再実行で使われるロールフォワード-多重化できる)
    - [一つのデータベースに複数のUNDO表領域を作ることはできるが、使用できるUNDO表領域は1つだけ。](#一つのデータベースに複数のundo表領域を作ることはできるが使用できるundo表領域は1つだけ)
    - [UNDOセグメントの所有者はPUBLIC](#undoセグメントの所有者はpublic)
    - [1. UNDO\_MANAGEMENT初期化パラメータをAUTOにする 2.CREATE UNDO TABLESPACE文でUNDO表領域を作成する 3. UNDO\_TABLESPACE初期化パラメータにUNDO表領域の名前を設定する。](#1-undo_management初期化パラメータをautoにする-2create-undo-tablespace文でundo表領域を作成する-3-undo_tablespace初期化パラメータにundo表領域の名前を設定する)
  - [LIKEで指定する検索条件: `_`: 任意の一文字, `%`: 任意の文字列](#likeで指定する検索条件-_-任意の一文字--任意の文字列)
  - [ORDER BYで昇順で並べ替えた時、NULLは末尾、降順の時はNULLは先頭になる。](#order-byで昇順で並べ替えた時nullは末尾降順の時はnullは先頭になる)
  - [置換変数`&`と`&&`の挙動の違い](#置換変数との挙動の違い)
  - [日時+日時の演算はできない](#日時日時の演算はできない)
  - [集計ファンクションをネストする時は、GROUP BY句が必要](#集計ファンクションをネストする時はgroup-by句が必要)

# 知見
## DESCはDESCRIBEコマンドの省略形
`DESCRIBE`,`DESC`は、指定したテーブルの「カラム名」「NULLがOKか否か」「型」を取得できます。
```sql
DESCRIBE <テーブル名>;
DESCRIBE INVENTORY;
DESC INVENTORY;
```

## `''`で囲った文字列の内部で、単なる文字列としてシングルクォートを使いたい場合の対処
`'`を`'あいう'えお'`とした時に、`あいう'えお`という文字列として扱いたいなどといった場合は、
- 文字列として扱いたいシングルクォートを2つ繋げて書く→`'あいう''えお'`
- `q'<任意の一文字>.*<任意の一文字>'`というように、`q'<任意の一文字>`と`<任意の一文字>'`で文字列を囲う。→`q'#あいう'えお#'`, `q'|あいう'えお|'`<br>
<任意の一文字>は、同じ文字でなくても良いので、`q'(あいう'えお)'`, `q'<あいう'えお>'`でも良い。

## 列の別名に`$`, `_`, `#`以外の記号を含むとき（` `, `-`など）は、文字列を`""`で囲う

# 要注意！落とし穴リスト
## 4章 リスナー, サービス登録
### リスナーの動的サービス登録するには、LOCAL_LISTENER初期化パラメータを設定すること
LREG(リスナー登録プロセス)が、自動でリスナーの管理を行うようになる。インスタンスが停止するとリスナーからサービス登録が解除される。
### 動的サービス登録で、デフォルト値でリスナーを起動させるならlistener.oraは不要
### リスナーの静的サービス登録するには、listener.oraのSID_LIST_<リスナー名>パラメータにデータベース情報を記述しておくこと。
これによってリスナーが常にサービス登録されている状態になり、`lsnrctl status`を実行すると、インスタンスの状態が`UNKNOWN`になる。
### Oracle Database Gatewayを使って外部の別のDBMS製品とデータ連携できる（異機種間サービス）

## 5章 権限の管理
### 「セグメント作成の遅延」が無効→クオータ（表領域の割り当て制限）が割り当てられていないと、表や索引などセグメントを伴うオブジェクトを作成することができない
### 「セグメント作成の遅延」が有効→クオータが割り当てられていなくても、表や索引などセグメントを伴うオブジェクトを作成することができる。
### システム権限の付与→WITH ADMIN OPTION, オブジェクト権限の付与→WITH GRANT OPTION
### 権限を取り消すときは、一旦権限付与を取り消してから、WITH ADMIN OPTIONなしで再度権限を付与する。
`REVOKE CREATE TABLE FROM SMITH WITH ADMIN OPTION`のように、REVOKE文にWITH ADMIN OPTIONは付けられない。
### PUBLICロールに権限付与すると、すべてのOracleユーザでその権限が有効になる。
### SET ROLE文で、そのセッションにおけるロールの有効/無効を切り替えることができる。
### 権限分析(DBMS_PRIVILEGE_CAPTURE)パッケージで権限分析対象にできるのは、「データベース全体」「ロール」「コンテキスト」「ロール＋コンテキスト」の4パターン
ただし、SYSユーザだけは分析対象にできない。
ロールは、指定したロールが付与されているユーザを分析できる。コンテキストは指定した評価式に合致したユーザ。ロール＋コンテキストはこれらの組み合わせになる。
### 管理権限を持つユーザー⇒パスワードファイル認証, それ以外の一般ユーザー⇒データディクショナリでログイン情報が管理されている。
一般ユーザをパスワードファイル認証にすることはできない。
### 一般ユーザでもOS認証に切り替えることができるが、Oracleユーザ名にOS_AUTHENT_PREFIX初期化パラメータで指定した接頭辞を付けたものを使用してCREATE USER文にIDENTIFIED EXTERNALLY句を付けて実行する。
`CREATE USER ops$u2 IDENTIFIED EXTERNALLY`, OS認証したいときは「IDENTIFIED EXTERNALLY句」を付ける。
### UNLIMITED TABLESPACEシステム権限だけは特別で、ロールに付与することができない。
UNLIMITED TABLESPACEシステム権限が付与されたユーザはすべての表領域について無制限に記憶域を割り当てることができるようになる。<br>
一方で、ロールに付与することはできない。

## 6章 表領域の概念
### SYSTEM表領域が使用できなくなった場合、インスタンスは異常終了する
### データベースデフォルトの表領域のブロックサイズは変更できない
データベースデフォルトの表領域のブロックサイズ(標準ブロックサイズ)は変更することができない。<br>
しかし、「非標準ブロックサイズ」と呼ばれる、データベースのデフォルトのブロックサイズとは異なるブロックサイズを使用する表領域を作ることはできる。<br>
その場合、表領域を作成する前に、`DB_nK_CACHE_SIZE`という初期化パラメータ(n=2,4,8,16,32)を設定し、<br>
使用するブロックサイズに応じたデータベースバッファキャッシュを構成しておく必要がある。<br>
たとえば、16Kバイトのブロックサイズで表領域を作成したい場合は、`DB_16K_CACHE_SIZE`のように初期化パラメータを設定すること。

### Oracleのエクステント管理方式は、ほとんどの場合「ローカル管理方式」が採用されている
エクステント管理方式とは、表領域を構成するデータファイルにおいて、どの領域をどのエクステントに割り当てているのか、どの領域が空いているのかを管理する方法です。<br>
※ディレクショナリ管理方式は、非推奨となる予定。

### エクステントの管理方式 ローカル管理方式⇒表領域の管理用ブロックでエクステントを管理している(高速), ディクショナリ管理方式⇒データディクショナリ(SYSTEM表領域)でエクステントを管理している。(低速)

### セグメント割り当てタイプは、UNDO表領域⇒AUTOALLOCATE, 一時表領域⇒UNIFORMで固定
### bigfile表領域のデータファイル数は1で固定
### DROP TABLESPACE mytbs; (空の表領域を削除できる)(アクティブなトランザクションがあると、表領域は削除できない)
### DROP TABLESPACE mytbs INCLUDING CONTENTS; (表領域と一緒にオブジェクトも削除する(OMF管理だとデータファイルも消える))
### DROP TABLESPACE mytbs INCLUDING CONTENTS AND DATAFILES; (表領域と一緒にオブジェクトとデータファイルを削除する)
### OMF管理とは、データベースファイル管理をOracleで自動化する機能で、データファイル名が自動で決まる。不要なデータファイルは削除される。
### DB_CREATE_FILE_DEST, DB_CREATE_ONLINE_LOG_DEST_n, DB_RECOVERY_FILE_DEST ⇒OMF管理に関する問題ですよ。ということ。

## 7章 表領域
### ローカル管理方式でできること⇒エクステント割り当ての自動化
### 自動セグメント領域管理⇒SEGMENT MANAGEMENT AUTO句を指定すると自動セグメント領域管理方式が使用できる。
```sql:
CREATE TABLESPACE tbs_auto
  DATAFILE '/u01/oradata/tbs_auto01.dbf' SIZE 100M
  SEGMENT MANAGEMENT AUTO; /* 自動セグメント領域管理方式「AUTO」（手動管理にしたい時は「MANUAL」を指定する） */
```

### 再開可能文4選⇒(1)一時表領域を使うSELECT文,(2)DML,(3)ロード系処理(Data Pump,SQL*Loader),(4)DDL:create table as select等
なお、再開可能文が一時停止すると、
・エラーがアラートログに記録される。<br>
・ユーザーがAFTER SUSPENDイベントに対してトリガーを登録していた場合は、そのトリガーが実行される。<br>

### 再開可能領域割当てが動作する状況
・領域不足<br>
・クオータ割り当て超過（ユーザーの領域割り当て上限の超過）<br>
・セグメントが最大エクステント数に到達⇒ストレージに十分空きがあっても再開可能割り当てが発生することがありますよ。ということ。<br>

### 再開可能領域割り当ての機能を使うためには⇒RESUMABLE_TIMEOUT初期化パラメータを設定する
なお、セッションレベルで初期化パラメータを変更しても利用可能になる。
```sql:
ALTER SESSION ENABLE RESUMABLE TIMEOUT 100; /* 100秒間処理を一時停止する */
```

### ALTER TABLE SHRINK SPACE（セグメント縮小）するためには、行移動を有効化(ENABLE ROW MOVEMENT)すること。
`ALTER TABLE emp ENABLE ROW MOVEMENT;`
### ALTER TABLE SHRINK SPACE文でセグメントを縮小して、HWMの位置を変更できる。
### ALTER TABLE emp SHRINK SPACE; ⇒セグメント内データの再編成（片側に寄せる処理）セグメントの縮小+HWMの引き下げ
### ALTER TABLE emp SHRINK SPACE COMPACT; ⇒片側に寄せる処理+HWMの引き下げ⇒COMPACTは、片側に寄せる処理コンパクトにする処理だけを実行するということ。
### ALTER TABLE emp SHRINK SPACE CASCADE; ⇒片側に寄せる処理+セグメントの縮小+HWMの引き下げ+索引にも縮小処理をする。
### ALTER INDEX ... SHRINK SPACE [...]; ⇒を使えば、索引だけピンポイントで縮小処理をすることもできる。
### TRUNCATE TABLEを実行すると、全行削除するだけではなく、エクステントを解放するかどうかについても制御できる。
### TRUNCATE TABLE tbl01; --デフォルトではDROP STORAGEと同じ
### TRUNCATE TABLE tbl01 DROP STORAGE; 表の作成後に追加で作成されたエクステントを解放する。
### TRUNCATE TABLE tbl01 DROP ALL STORAGE; すべてのエクステントを解放する。
### TRUNCATE TABLE tbl01 REUSE STORAGE; エクステントを解放せずに使い続ける。
### セグメント縮小⇒ALTER TABLE ... SHRINK SPACE [COMPACT] [CASCADE]
### TRUNCATE TABLE ... ⇒エクステントの解放
### 表圧縮⇒ROW STORE COMPRESS BASIC, ROW STORE COMPRESS, COMPRESS, ROW STORE COMPRESS ADVANCEDのこと。

## 8章 UNDO表領域
### UNDO⇒過去データが必要な処理（読取り一貫性の保証, ロールバック, 多重化できない, フラッシュバック機能）
### REDO⇒障害からの復旧時の再実行で使われる（ロールフォワード, 多重化できる）
### 一つのデータベースに複数のUNDO表領域を作ることはできるが、使用できるUNDO表領域は1つだけ。
### UNDOセグメントの所有者はPUBLIC
### 1. UNDO_MANAGEMENT初期化パラメータをAUTOにする 2.CREATE UNDO TABLESPACE文でUNDO表領域を作成する 3. UNDO_TABLESPACE初期化パラメータにUNDO表領域の名前を設定する。

## LIKEで指定する検索条件: `_`: 任意の一文字, `%`: 任意の文字列
## ORDER BYで昇順で並べ替えた時、NULLは末尾、降順の時はNULLは先頭になる。
## 置換変数`&`と`&&`の挙動の違い
- `&empno`が呼ばれる度に値の入力を求められる。: `&empno`
- `&&empno`が呼ばれるのは一度だけで、最初に入力した値が使いまわされる。: `&&empno`

## 日時+日時の演算はできない
例えば、以下のような処理はエラーとなる。
```sql
SELECT TO_DATE('2021/01/02 00:00:00') + TO_DATE('2021/01/01 00:00:00') FROM DUAL;
```

```
行1でエラーが発生しました。:
ORA-00975: 日付と日付の加算はできません。
```

## 集計ファンクションをネストする時は、GROUP BY句が必要
下のsqlは一見よさそうだが、実はエラーになる。
```sql
SELECT MAX(AVG(sal)) FROM emp;
```

集計ファンクションをネストして利用する時は、1段階目の集計処理を複数行にするために、<br>
`GROUP BY`でグループ化しておいて、2段階目の集計処理にグループ化したテーブルの処理が走る。
```sql
SELECT MAX(AVG(sal)) FROM emp GROUP BY deptno;
```

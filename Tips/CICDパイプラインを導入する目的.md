# CI/CDを導入する目的


# CI/CDで検討される処理とは
CI/CDパイプラインで検討される処理は、単に「ビルドしてデプロイする」だけではなく、**コード変更を検知してから、本番環境へ安全にリリースするまでの一連の品質保証・自動化処理**として考えると整理しやすいです。

Java + GitLab CIのような環境を想定すると、一般的には次のような処理を検討します。
- 1. ソースコード取得
- 2. 静的解析・コード品質チェック
- 3. 依存ライブラリのチェック
- 4. コンパイル・ビルド
- 5. 単体テスト
- 6. テストカバレッジ測定
- 7. 成果物（Artifact）の生成・保存
- 8. Dockerイメージのビルド
- 9. Dockerイメージの脆弱性スキャン
- 10. 結合テスト
- 11. APIテスト・E2Eテスト
- 12. データベースマイグレーション
- 13. デプロイ
- 14. デプロイ後のヘルスチェック
- 15. Smoke Test
- 16. 承認（Approval）
- 17. 本番デプロイ
- 18. デプロイ後の監視
- 19. 自動Rollback


### 1. ソースコード取得

Gitリポジトリから対象ブランチのソースコードを取得します。

```text
Git Push
   ↓
CI/CD Pipeline開始
   ↓
ソースコードCheckout
```

GitLab CIならRunnerが自動的にリポジトリを取得するため、明示的な処理が不要な場合もあります。

---

### 2. 静的解析・コード品質チェック

コンパイルやテストの前に、ソースコードそのものをチェックします。

例えば、

* コーディング規約チェック
* 未使用コードの検出
* バグになりそうなコードの検出
* 脆弱性の検出
* コード品質の測定
* 複雑度の測定

などです。

代表的なツールとしては、

* SonarQube / SonarCloud
* SpotBugs
* Checkstyle
* PMD
* ESLint

などがあります。

---

### 3. 依存ライブラリのチェック

Mavenなどで使用しているライブラリについて、

* 脆弱性がないか
* 古すぎるバージョンではないか
* ライセンス上問題がないか

などをチェックします。

例えば、

```text
pom.xml
   ↓
依存ライブラリ取得
   ↓
脆弱性スキャン
   ↓
問題があればPipeline失敗
```

という流れです。

---

### 4. コンパイル・ビルド

Javaアプリケーションなら、

```bash
mvn clean package
```

などを実行します。

例えば、

```text
.java
 ↓
コンパイル
 ↓
.class
 ↓
WAR / JAR
```

となります。

CI/CDでは、**ビルドできなければ後続処理を実行しない**という設計が一般的です。

---

### 5. 単体テスト

JUnitなどを利用して自動テストを実行します。

```text
ソースコード
   ↓
コンパイル
   ↓
JUnit
   ↓
テスト結果
```

例えば、

```text
100 tests
98 passed
2 failed
```

となった場合、通常はPipelineを失敗させてデプロイまで進ませません。

---

### 6. テストカバレッジ測定

単体テストと合わせて、

* Line Coverage
* Branch Coverage
* Method Coverage

などを測定します。

例えば、

```text
Coverage = 85%
```

のようにして、一定値を下回ったらPipelineを失敗させることもあります。

JavaならJaCoCoがよく使われます。

---

### 7. 成果物（Artifact）の生成・保存

ビルドした、

```text
application.war
application.jar
```

などをCI/CDの成果物として保存します。

ここは非常に重要です。

例えば、

```text
Git Commit A
    ↓
Build
    ↓
app.war
    ↓
Artifactとして保存
```

としておけば、後で同じArtifactを使って、

```text
開発環境
 ↓
検証環境
 ↓
ステージング環境
 ↓
本番環境
```

へデプロイできます。

**環境ごとに再ビルドしない**という考え方が重要です。

---

### 8. Dockerイメージのビルド

Dockerを利用するプロジェクトなら、

```text
ソースコード
 ↓
Maven Build
 ↓
JAR
 ↓
Docker Image Build
 ↓
Container Registry
```

という処理も入ります。

例えば、

```text
my-app:1.2.3
```

のようなイメージを作成します。

---

### 9. Dockerイメージの脆弱性スキャン

作成したDockerイメージに対して、

* OSパッケージの脆弱性
* Javaランタイムの脆弱性
* 含まれるライブラリの脆弱性

などを検査します。

Trivyなどがよく使われます。

---

### 10. 結合テスト

単体テストの次に、実際のコンポーネントを組み合わせてテストします。

例えば、

```text
Application
    ↓
MySQL
    ↓
Redis
```

のような環境を作って、

```text
API → Service → Repository → DB
```

まで正常に動くか確認します。

Docker Composeなどを使ってCI環境を構築するケースもあります。

---

### 11. APIテスト・E2Eテスト

さらに実際の利用者に近い形で、

```text
HTTP Request
     ↓
Application
     ↓
HTTP Response
```

を確認します。

例えば、

```text
POST /users
GET /users/123
DELETE /users/123
```

などのAPIテストです。

WebアプリケーションならPlaywrightやSeleniumなどを使ったE2Eテストも考えられます。

---

### 12. データベースマイグレーション

DB変更がある場合は、

```text
Flyway
Liquibase
```

などをCI/CDに組み込むことがあります。

例えば、

```text
V1__create_user.sql
V2__add_email.sql
V3__add_index.sql
```

を自動適用します。

ただし、**本番DBへのマイグレーションを完全自動化するかどうか**は慎重に検討する必要があります。

---

### 13. デプロイ

ここで初めて実際のサーバーへデプロイします。

例えば現在の経験に近い構成なら、

```text
GitLab
  ↓
GitLab Runner
  ↓
Ant/Maven Build
  ↓
WAR
  ↓
SSH
  ↓
EC2
  ↓
Tomcat
```

のような流れです。

Dockerなら、

```text
GitLab
 ↓
Build
 ↓
Docker Image
 ↓
ECR
 ↓
ECS
```

のようになります。

---

### 14. デプロイ後のヘルスチェック

デプロイしただけで終了にはせず、

```text
Deploy
 ↓
Application起動
 ↓
Health Check
 ↓
正常？
```

を確認します。

例えば、

```http
GET /actuator/health
```

を実行して、

```json
{
  "status": "UP"
}
```

なら成功、といった判定です。

---

### 15. Smoke Test

最低限の主要機能が動くことを確認します。

例えば、

```text
ログイン
 ↓
トップ画面
 ↓
主要API
```

などです。

これによって、

> デプロイは成功したが、アプリケーションが実際には起動していない

という問題を検出できます。

---

### 16. 承認（Approval）

本番環境へのデプロイだけは自動ではなく、

```text
Deploy to Staging
        ↓
      Test
        ↓
   [Approval]
        ↓
Deploy to Production
```

とする構成も非常に一般的です。

特に金融・公共系などでは重要になります。

---

### 17. 本番デプロイ

承認後、

```text
Production Deploy
```

を実行します。

ここでは、

* Blue-Green Deployment
* Rolling Deployment
* Canary Release

などのデプロイ方式も検討対象になります。

---

### 18. デプロイ後の監視

本番デプロイ後に、

* CPU
* Memory
* Response Time
* HTTP 5xx
* Error Log
* Request数

などを監視します。

例えば、

```text
Deploy
 ↓
Health Check
 ↓
Metrics確認
 ↓
Error率確認
```

という処理です。

---

### 19. 自動Rollback

デプロイ後に異常が検知されたら、以前のバージョンへ戻します。

```text
v1.2.0
 ↓
Deploy
 ↓
Error率急増
 ↓
Rollback
 ↓
v1.1.9
```

という仕組みです。

---

## 全体像

CI/CDをかなり一般化すると、次のようになります。

```text
                    Git Push
                       │
                       ▼
              ┌─────────────────┐
              │ Source Checkout │
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Static Analysis │
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Dependency Scan │
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Build / Compile │
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Unit Test       │
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Coverage        │
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Integration Test│
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Artifact Build  │
              └────────┬────────┘
                       ▼
              ┌─────────────────┐
              │ Security Scan   │
              └────────┬────────┘
                       ▼
                 Deploy to Dev
                       │
                       ▼
                 E2E / Smoke
                       │
                       ▼
                 Deploy to Stg
                       │
                       ▼
                    Approval
                       │
                       ▼
                 Deploy to Prod
                       │
                       ▼
               Health Check
                       │
                 ┌─────┴─────┐
                 ▼           ▼
              Success      Failure
                 │           │
                 ▼           ▼
             Monitoring   Rollback
```

## 実務では「全部入れる」わけではありません

ここが重要です。

CI/CDの設計では、上記を全部自動化するというより、

**「どの段階で何を検査し、失敗したらどこで止めるか」**

を決めることが重要です。

例えばJava Webアプリケーションなら、最初は、

| Stage | 処理              | 失敗時   |
| ----- | ----------------- | -------- |
| 1     | Checkout          | Stop     |
| 2     | 静的解析          | Stop     |
| 3     | Maven Build       | Stop     |
| 4     | Unit Test         | Stop     |
| 5     | Coverage          | Stop     |
| 6     | Artifact生成      | Stop     |
| 7     | Dev Deploy        | Stop     |
| 8     | Integration Test  | Stop     |
| 9     | Staging Deploy    | Stop     |
| 10    | E2E / Smoke Test  | Stop     |
| 11    | Approval          | 手動     |
| 12    | Production Deploy | Stop     |
| 13    | Health Check      | Rollback |

くらいから設計すると理解しやすいです。

特に、**あなたがこれまで経験されている「GitLab CI/CDパイプライン構築」について転職面接で説明する場合**は、「GitLab CIでビルドを自動化しました」だけではなく、**「どの品質ゲートを設け、どの処理を自動化し、失敗時にどう制御する設計にしたか」**まで説明できると、CI/CDを設計した経験としてかなり強くなります。


# まとめ
- 1. ソースコード取得
- 2. 静的解析・コード品質チェック(SonarQube/SonarCloud, SpotBugs, Checkstvleを利用する)
- 3. 依存ライブラリのチェック(ライブラリに脆弱性が無いか,ライブラリが古すぎないか,ライセンスに問題はないか)
- 4. コンパイル・ビルド
- 5. 単体テスト
- 6. テストカバレッジ測定(JavaならJaCoCoが良く使われる)
- 7. 成果物（Artifact）の生成・保存(jar,warをCI/CDの成果物として保存する)
- 8. Dockerイメージのビルド
- 9. Dockerイメージの脆弱性スキャン
- 10. 結合テスト
- 11. APIテスト・E2Eテスト(PlaywrightやSeleniumなどを使って行う)
- 12. データベースマイグレーション
- 13. デプロイ
- 14. デプロイ後のヘルスチェック
- 15. Smoke Test(最低限の主要機能が動いていることの確認)
- 16. 承認（Approval）
- 17. 本番デプロイ
- 18. デプロイ後の監視
- 19. 自動Rollback

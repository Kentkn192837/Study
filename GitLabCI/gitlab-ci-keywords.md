# .gitlab-ci.yml キーワード完全ガイド

## 概要
`.gitlab-ci.yml`ファイルで使用される主要なキーワードとその役割を詳しく解説します。

## 基本構造

```yaml
# .gitlab-ci.yml の基本構造

# グローバル設定
stages:          # パイプラインのステージ定義
variables:       # 環境変数の定義
image:          # デフォルトDockerイメージ

# ジョブ定義
job_name:       # ジョブ名（任意の名前）
  stage:        # どのステージで実行するか
  image:        # 使用するDockerイメージ
  script:       # 実行するコマンド
  before_script: # メインスクリプトの前に実行
  after_script:  # メインスクリプトの後に実行（成功・失敗問わず）
```

## 主要キーワード詳細解説

### 1. **stages** - ステージ定義

```yaml
stages:
  - build     # ビルド段階
  - test      # テスト段階  
  - deploy    # デプロイ段階
```

**役割**: パイプラインの実行順序を定義
- ステージは**順次実行**される
- 同じステージ内のジョブは**並列実行**される
- 前のステージが失敗すると、次のステージは実行されない

**実例**:
```yaml
stages:
  - prepare    # 準備（依存関係インストール等）
  - build      # ビルド
  - test       # テスト
  - security   # セキュリティスキャン
  - deploy     # デプロイ
  - cleanup    # 後処理
```

### 2. **image** - Dockerイメージ指定

```yaml
# グローバル設定（全ジョブのデフォルト）
image: node:18

# ジョブ固有設定
build_job:
  image: maven:3.9.4-eclipse-temurin-17
  script:
    - mvn compile
```

**役割**: ジョブを実行するDockerコンテナのベースイメージを指定
- **グローバル**: 全ジョブに適用されるデフォルト
- **ジョブレベル**: そのジョブでのみ使用

**よく使用されるイメージ**:
```yaml
# プログラミング言語別
image: node:18              # Node.js
image: python:3.9           # Python
image: openjdk:17          # Java
image: maven:3.9.4-eclipse-temurin-17  # Maven + Java
image: php:8.1             # PHP

# OS系
image: ubuntu:22.04        # Ubuntu
image: alpine:latest       # Alpine Linux（軽量）

# ツール系
image: docker:20.10.16     # Docker
image: amazon/aws-cli      # AWS CLI
```

### 3. **script** - 実行コマンド

```yaml
build:
  stage: build
  script:
    - echo "Starting build..."
    - npm install
    - npm run build
    - echo "Build completed!"
```

**役割**: ジョブで実行するメインコマンドを定義
- **配列形式**で複数のコマンドを記述
- コマンドは**順次実行**される
- 1つでも失敗するとジョブが失敗

**高度な使用例**:
```yaml
complex_script:
  script:
    # 条件分岐
    - |
      if [ "$CI_COMMIT_BRANCH" = "main" ]; then
        echo "Production build"
        npm run build:prod
      else
        echo "Development build"
        npm run build:dev
      fi
    
    # 複数行コマンド
    - |
      for file in *.txt; do
        echo "Processing $file"
        process_file "$file"
      done
    
    # エラーハンドリング
    - npm test || (echo "Tests failed" && exit 1)
```

### 4. **before_script / after_script** - 前後処理

```yaml
job_name:
  before_script:
    - echo "準備処理開始"
    - apt-get update
    - apt-get install -y curl
  script:
    - echo "メイン処理"
    - ./run-main-process.sh
  after_script:
    - echo "後処理（ログ収集等）"
    - cp logs/* /artifacts/
```

**before_script**:
- **メインスクリプトの前**に実行
- 環境準備、依存関係インストール等に使用
- 失敗するとジョブ全体が失敗

**after_script**:
- **メインスクリプトの後**に必ず実行（成功・失敗問わず）
- ログ収集、クリーンアップ等に使用
- 失敗してもジョブの成功・失敗に影響しない

### 5. **variables** - 環境変数

```yaml
# グローバル変数
variables:
  NODE_VERSION: "18"
  BUILD_ENV: "production"
  DOCKER_DRIVER: overlay2

# ジョブ固有変数
build:
  variables:
    MAVEN_OPTS: "-Xmx1024m"
  script:
    - echo "Node version: $NODE_VERSION"
    - echo "Maven options: $MAVEN_OPTS"
```

**役割**: ジョブ内で使用する環境変数を定義

**GitLab定義済み変数（CI_で始まる）**:
```yaml
script:
  - echo "Project name: $CI_PROJECT_NAME"
  - echo "Commit SHA: $CI_COMMIT_SHA"
  - echo "Branch: $CI_COMMIT_REF_NAME"
  - echo "Pipeline ID: $CI_PIPELINE_ID"
  - echo "Job name: $CI_JOB_NAME"
```

### 6. **rules** - 実行条件

```yaml
# ブランチ条件
deploy:
  script: echo "Deploying..."
  rules:
    - if: $CI_COMMIT_BRANCH == "main"

# 複数条件
build:
  script: echo "Building..."
  rules:
    - if: $CI_COMMIT_BRANCH == "main"        # mainブランチ
    - if: $CI_COMMIT_BRANCH == "develop"     # developブランチ
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"  # MR時

# 条件 + 手動実行
manual_deploy:
  script: echo "Manual deployment"
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
      when: manual    # 手動実行必須
```

**よく使用される条件**:
```yaml
rules:
  # ブランチ指定
  - if: $CI_COMMIT_BRANCH == "main"
  - if: $CI_COMMIT_BRANCH =~ /^release\/.*$/    # release/で始まるブランチ
  
  # パイプラインソース
  - if: $CI_PIPELINE_SOURCE == "push"           # プッシュ時
  - if: $CI_PIPELINE_SOURCE == "merge_request_event"  # MR時
  - if: $CI_PIPELINE_SOURCE == "schedule"       # スケジュール実行時
  
  # ファイル変更
  - changes:
      - "src/**/*"     # srcディレクトリ内のファイルが変更された時
      - "*.js"         # JSファイルが変更された時
  
  # 存在チェック
  - exists:
      - Dockerfile     # Dockerfileが存在する時
```

### 7. **artifacts** - 成果物管理

```yaml
build:
  script:
    - npm run build
  artifacts:
    paths:
      - dist/          # 保存するディレクトリ
      - logs/*.log     # ログファイル
    expire_in: 1 week  # 保存期間
    when: on_success   # 成功時のみ保存
```

**詳細設定**:
```yaml
test:
  script:
    - npm test
  artifacts:
    # 保存するファイル・ディレクトリ
    paths:
      - coverage/
      - test-results.xml
    
    # 保存期間
    expire_in: 30 days
    
    # 保存条件
    when: always        # 常に保存
    # when: on_success  # 成功時のみ
    # when: on_failure  # 失敗時のみ
    
    # レポート（GitLabが解析）
    reports:
      junit: test-results.xml           # JUnitテスト結果
      coverage_report:                  # カバレッジレポート
        coverage_format: cobertura
        path: coverage/cobertura.xml
```

### 8. **cache** - キャッシュ

```yaml
# グローバルキャッシュ
cache:
  key: $CI_COMMIT_REF_SLUG    # ブランチごとにキャッシュ
  paths:
    - node_modules/
    - .npm/

# ジョブ固有キャッシュ
build:
  cache:
    key: 
      files:
        - package-lock.json   # このファイルが変更されたらキャッシュ更新
    paths:
      - node_modules/
    policy: pull-push         # 読み込み＋書き込み
```

**キャッシュポリシー**:
```yaml
cache:
  policy: pull-push   # デフォルト：読み込み＋書き込み
  # policy: pull      # 読み込みのみ
  # policy: push      # 書き込みのみ
```

### 9. **services** - サービスコンテナ

```yaml
integration_test:
  image: node:18
  services:
    - postgres:13
    - redis:6
  variables:
    POSTGRES_DB: testdb
    POSTGRES_USER: test
    POSTGRES_PASSWORD: test
  script:
    - npm run test:integration
```

**役割**: テスト用データベース等の**依存サービス**を起動

### 10. **needs** - 依存関係

```yaml
build:
  stage: build
  script: echo "Building..."

test:
  stage: test
  needs: ["build"]    # buildジョブ完了後に実行
  script: echo "Testing..."

deploy:
  stage: deploy
  needs:
    - job: build
      artifacts: true  # buildの成果物が必要
  script: echo "Deploying..."
```

**効果**: ステージを跨いだ**並列実行**が可能

## 実践的な設定例

### Node.jsプロジェクト完全版

```yaml
# 全体設定
stages:
  - build
  - test
  - security
  - deploy

variables:
  NODE_VERSION: "18"
  NPM_CONFIG_CACHE: ".npm"

# デフォルトイメージ
image: node:$NODE_VERSION

# グローバルキャッシュ
cache:
  key:
    files:
      - package-lock.json
  paths:
    - node_modules/
    - .npm/

# ビルドジョブ
build:
  stage: build
  before_script:
    - node --version
    - npm --version
  script:
    - npm ci
    - npm run build
  artifacts:
    paths:
      - dist/
    expire_in: 1 hour
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# テストジョブ
test:unit:
  stage: test
  needs: ["build"]
  script:
    - npm ci
    - npm run test:unit
  artifacts:
    reports:
      junit: junit.xml
      coverage_report:
        coverage_format: cobertura
        path: coverage/cobertura.xml
  coverage: '/Lines\s*:\s*(\d+\.\d+)%/'

# セキュリティスキャン
security:
  stage: security
  image: node:$NODE_VERSION
  script:
    - npm audit --audit-level=high
  allow_failure: true

# 本番デプロイ
deploy:production:
  stage: deploy
  image: alpine:latest
  needs:
    - job: build
      artifacts: true
  before_script:
    - apk add --no-cache openssh-client
  script:
    - echo "Deploying to production..."
    - scp -r dist/ user@server:/var/www/
  environment:
    name: production
    url: https://example.com
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
      when: manual
```

### Javaプロジェクト完全版

```yaml
stages:
  - build
  - test
  - package
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  MAVEN_CLI_OPTS: "--batch-mode --errors --fail-at-end --show-version"

image: maven:3.9.4-eclipse-temurin-17

cache:
  key: 
    files:
      - pom.xml
  paths:
    - .m2/repository/

build:
  stage: build
  script:
    - mvn $MAVEN_CLI_OPTS compile
  artifacts:
    paths:
      - target/classes/
    expire_in: 1 hour

test:
  stage: test
  needs: ["build"]
  script:
    - mvn $MAVEN_CLI_OPTS test
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml

package:
  stage: package
  needs: ["test"]
  script:
    - mvn $MAVEN_CLI_OPTS package -DskipTests
  artifacts:
    paths:
      - target/*.jar
    expire_in: 1 week

deploy:
  stage: deploy
  image: alpine:latest
  needs:
    - job: package
      artifacts: true
  script:
    - echo "Deploying JAR file..."
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
      when: manual
```

## よくある間違いと注意点

### 1. **インデント**
```yaml
# ❌ 間違い：インデントが不正
build:
stage: build    # インデントが足りない
  script:
- echo "test"   # インデントが不正

# ✅ 正しい：適切なインデント（2スペース）
build:
  stage: build
  script:
    - echo "test"
```

### 2. **文字列の引用符**
```yaml
# ❌ 間違い：特殊文字を含む文字列
script:
  - echo $CI_COMMIT_BRANCH == "main"    # 解析エラーの可能性

# ✅ 正しい：引用符で囲む
script:
  - echo "$CI_COMMIT_BRANCH == main"
```

### 3. **rulesの記述**
```yaml
# ❌ 間違い：onlyとrulesの混在
deploy:
  script: echo "deploy"
  only:
    - main
  rules:    # onlyと同時使用は不可
    - if: $CI_COMMIT_BRANCH == "main"

# ✅ 正しい：rulesのみ使用
deploy:
  script: echo "deploy"
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

これらのキーワードを理解することで、効率的で安全なCI/CDパイプラインを構築できます！

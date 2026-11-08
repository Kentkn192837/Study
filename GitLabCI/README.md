# GitLab CI/CD デプロイ自動化ガイド

## 目次
1. [GitLab CI/CDとは](#gitlab-cicdとは)
2. [基本的な仕組み](#基本的な仕組み)
3. [`.gitlab-ci.yml`の基本構造](#gitlab-ciymlの基本構造)
4. [基本的なパイプラインの例](#基本的なパイプラインの例)
5. [デプロイの種類](#デプロイの種類)
6. [実践的な例](#実践的な例)
7. [ベストプラクティス](#ベストプラクティス)

## GitLab CI/CDとは

GitLab CI/CD（Continuous Integration/Continuous Deployment）は、GitLabが提供する継続的インテグレーションと継続的デプロイメントのためのツールです。

### 主な機能
- **継続的インテグレーション（CI）**: コードの変更を自動的にテスト
- **継続的デプロイメント（CD）**: テストに通過したコードを自動的にデプロイ
- **パイプライン**: 一連のジョブを順序立てて実行
- **ランナー**: 実際にジョブを実行するエージェント

## 基本的な仕組み

```
コミット/プッシュ → パイプライン起動 → ジョブ実行 → デプロイ
```

1. **トリガー**: コードのプッシュ、マージリクエスト等でパイプライン起動
2. **ステージ**: build、test、deploy等の段階に分けて実行
3. **ジョブ**: 各ステージで実行される具体的なタスク
4. **ランナー**: ジョブを実行するマシン（GitLab提供またはセルフホスト）

## .gitlab-ci.ymlの基本構造

GitLab CI/CDの設定は、リポジトリルートに配置する`.gitlab-ci.yml`ファイルで定義します。

### 基本的な構文

```yaml
# ステージの定義
stages:
  - build
  - test
  - deploy

# 変数の定義
variables:
  NODE_VERSION: "18"

# ジョブの定義
job_name:
  stage: build
  script:
    - echo "Hello World"
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

### 主要なキーワード

- `stages`: パイプラインのステージを定義
- `script`: 実行するコマンドを定義
- `image`: 使用するDockerイメージ
- `before_script`: ジョブの前に実行するスクリプト
- `after_script`: ジョブの後に実行するスクリプト
- `rules`: ジョブの実行条件
- `only`/`except`: 古い形式の実行条件（非推奨）
- `artifacts`: ジョブ間でファイルを受け渡し
- `cache`: 依存関係等をキャッシュ

## 基本的なパイプラインの例

### シンプルなNode.jsアプリケーション

```yaml
stages:
  - build
  - test
  - deploy

variables:
  NODE_VERSION: "18"

# ビルドジョブ
build:
  stage: build
  image: node:$NODE_VERSION
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
test:
  stage: test
  image: node:$NODE_VERSION
  script:
    - npm ci
    - npm test
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# デプロイジョブ
deploy:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache rsync openssh
  script:
    - rsync -avz --delete dist/ user@server:/var/www/html/
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH
  environment:
    name: production
    url: https://example.com
```

## デプロイの種類

### 1. 手動デプロイ
```yaml
deploy:
  stage: deploy
  script:
    - echo "Deploying to production"
  when: manual
  environment:
    name: production
```

### 2. 自動デプロイ（mainブランチのみ）
```yaml
deploy:
  stage: deploy
  script:
    - echo "Auto deploying to production"
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  environment:
    name: production
```

### 3. 環境別デプロイ
```yaml
deploy_staging:
  stage: deploy
  script:
    - echo "Deploying to staging"
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"
  environment:
    name: staging
    url: https://staging.example.com

deploy_production:
  stage: deploy
  script:
    - echo "Deploying to production"
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  environment:
    name: production
    url: https://example.com
```

## 実践的な例

次のファイルで具体的な実装例を詳しく説明します：
- [Docker デプロイ例](./docker-deploy-example.md)
- [AWS デプロイ例](./aws-deploy-example.md)  
- [**Tomcat + EC2 デプロイ例**](./tomcat-ec2-deploy.md) - 従来のJava Web環境
- [Tomcat CI/CD 段階的導入](./tomcat-phased-approach.md) - 段階的導入計画
- [**Docker不要でのCI/CD**](./docker-alternatives.md) - Shell ExecutorでのCI/CD
- [GitLab CI/CD キーワード解説](./gitlab-ci-keywords.md) - .gitlab-ci.ymlの書き方
- [Kubernetes デプロイ例](./kubernetes-deploy-example.md)

## ベストプラクティス

1. **セキュリティ**
   - 機密情報は環境変数で管理
   - デプロイキーを適切に設定

2. **効率性**
   - キャッシュを活用して実行時間を短縮
   - 並列実行を活用

3. **保守性**
   - YAMLファイルを分割して管理
   - テンプレートを活用

4. **モニタリング**
   - デプロイ後の動作確認を自動化
   - ロールバック機能を用意

詳細は各例のファイルを参照してください。

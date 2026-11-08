# GitLab CI/CD ベストプラクティス

## パフォーマンス最適化

### キャッシュ活用

```yaml
# Node.jsプロジェクトのキャッシュ例
.node_cache: &node_cache
  cache:
    key: 
      files:
        - package-lock.json
    paths:
      - node_modules/
    policy: pull-push

build:
  <<: *node_cache
  script:
    - npm ci
    - npm run build
```

### 並列実行の活用

```yaml
# テストの並列実行
test:unit:
  stage: test
  script:
    - npm run test:unit
  parallel:
    matrix:
      - NODE_VERSION: ["16", "18", "20"]

test:integration:
  stage: test
  script:
    - npm run test:integration
  parallel: 3  # 3つのジョブに分割
```

### Docker Layer キャッシュ

```yaml
build_docker:
  stage: build
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  variables:
    DOCKER_BUILDKIT: 1
  script:
    - docker build --cache-from $CI_REGISTRY_IMAGE:cache --build-arg BUILDKIT_INLINE_CACHE=1 -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker tag $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA $CI_REGISTRY_IMAGE:cache
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - docker push $CI_REGISTRY_IMAGE:cache
```

## セキュリティのベストプラクティス

### 機密情報管理

```yaml
# 環境変数の適切な使用
deploy:
  stage: deploy
  script:
    # ✅ 良い例：環境変数を使用
    - echo "Deploying to $ENVIRONMENT"
    - curl -H "Authorization: Bearer $API_TOKEN" https://api.example.com/deploy
  variables:
    ENVIRONMENT: "production"
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

### イメージの脆弱性スキャン

```yaml
security_scan:
  stage: test
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  script:
    # Trivyを使用した脆弱性スキャン
    - docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  allow_failure: true  # 脆弱性が見つかってもパイプラインを継続
```

### SAST（Static Application Security Testing）

```yaml
include:
  - template: Security/SAST.gitlab-ci.yml

# カスタムSAST設定
sast:
  variables:
    SAST_EXCLUDED_PATHS: "spec, test, tests, tmp"
```

## 環境管理

### 環境別設定の管理

```yaml
# テンプレートを使用した環境別デプロイ
.deploy_template: &deploy_template
  stage: deploy
  image: alpine:latest
  script:
    - echo "Deploying to $ENVIRONMENT"
    - ./deploy.sh $ENVIRONMENT
  
deploy_staging:
  <<: *deploy_template
  variables:
    ENVIRONMENT: staging
  environment:
    name: staging
    url: https://staging.example.com
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"

deploy_production:
  <<: *deploy_template
  variables:
    ENVIRONMENT: production
  environment:
    name: production
    url: https://example.com
  when: manual
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

### 動的環境の作成

```yaml
# プレビュー環境の動的作成
deploy_review:
  stage: deploy
  script:
    - echo "Creating review environment for $CI_MERGE_REQUEST_SOURCE_BRANCH_NAME"
    - ./create-preview-env.sh
  environment:
    name: review/$CI_MERGE_REQUEST_SOURCE_BRANCH_NAME
    url: https://$CI_MERGE_REQUEST_SOURCE_BRANCH_NAME.preview.example.com
    on_stop: stop_review
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"

stop_review:
  stage: deploy
  script:
    - echo "Destroying review environment"
    - ./destroy-preview-env.sh
  environment:
    name: review/$CI_MERGE_REQUEST_SOURCE_BRANCH_NAME
    action: stop
  when: manual
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
```

## コード品質管理

### Code Coverage

```yaml
test:
  stage: test
  script:
    - npm run test:coverage
  coverage: '/Coverage: \d+\.\d+%/'
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: coverage/cobertura-coverage.xml
```

### Code Quality

```yaml
include:
  - template: Code-Quality.gitlab-ci.yml

code_quality:
  artifacts:
    reports:
      codequality: gl-code-quality-report.json
```

## エラーハンドリングとロールバック

### 自動ロールバック機能

```yaml
deploy:
  stage: deploy
  script:
    # 現在のバージョンを記録
    - PREVIOUS_VERSION=$(kubectl get deployment myapp -o jsonpath='{.spec.template.spec.containers[0].image}')
    - echo $PREVIOUS_VERSION > previous_version.txt
    # 新バージョンデプロイ
    - kubectl set image deployment/myapp myapp=$NEW_IMAGE
    - kubectl rollout status deployment/myapp --timeout=300s
  after_script:
    # デプロイ失敗時のロールバック
    - |
      if [ $CI_JOB_STATUS == 'failed' ]; then
        echo "Deployment failed, rolling back..."
        PREVIOUS_VERSION=$(cat previous_version.txt)
        kubectl set image deployment/myapp myapp=$PREVIOUS_VERSION
        kubectl rollout status deployment/myapp
      fi
  artifacts:
    when: on_failure
    paths:
      - previous_version.txt
```

### ヘルスチェック付きデプロイ

```yaml
deploy_with_healthcheck:
  stage: deploy
  script:
    - ./deploy.sh
    # ヘルスチェック
    - |
      for i in {1..30}; do
        if curl -f https://example.com/health; then
          echo "Health check passed"
          exit 0
        fi
        echo "Health check failed, retrying in 10 seconds... ($i/30)"
        sleep 10
      done
      echo "Health check failed after 30 attempts"
      exit 1
```

## モニタリングと通知

### Slack通知

```yaml
notify_slack:
  stage: notify
  image: alpine:latest
  before_script:
    - apk add --no-cache curl
  script:
    - |
      curl -X POST -H 'Content-type: application/json' \
        --data "{\"text\":\"🚀 Deployment completed successfully for $CI_PROJECT_NAME ($CI_COMMIT_REF_NAME)\"}" \
        $SLACK_WEBHOOK_URL
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  when: on_success

notify_slack_failure:
  stage: notify
  image: alpine:latest
  before_script:
    - apk add --no-cache curl
  script:
    - |
      curl -X POST -H 'Content-type: application/json' \
        --data "{\"text\":\"❌ Deployment failed for $CI_PROJECT_NAME ($CI_COMMIT_REF_NAME) - $CI_PIPELINE_URL\"}" \
        $SLACK_WEBHOOK_URL
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  when: on_failure
```

### パフォーマンスモニタリング

```yaml
performance_test:
  stage: test
  image: loadimpact/k6:latest
  script:
    - k6 run --out json=results.json performance-tests/load-test.js
  artifacts:
    reports:
      performance: results.json
```

## CI/CDパイプラインの最適化

### ステージの条件付き実行

```yaml
stages:
  - build
  - test
  - security
  - deploy
  - notify

variables:
  SKIP_SECURITY: "false"

security_scan:
  stage: security
  script:
    - echo "Running security scan"
  rules:
    - if: $SKIP_SECURITY == "false"
```

### 失敗時の自動リトライ

```yaml
deploy:
  stage: deploy
  script:
    - ./deploy.sh
  retry:
    max: 2
    when:
      - runner_system_failure
      - stuck_or_timeout_failure
```

## トラブルシューティング

### デバッグ情報の収集

```yaml
debug:
  stage: build
  script:
    - echo "CI_COMMIT_SHA: $CI_COMMIT_SHA"
    - echo "CI_COMMIT_REF_NAME: $CI_COMMIT_REF_NAME"
    - echo "CI_PIPELINE_SOURCE: $CI_PIPELINE_SOURCE"
    - printenv | grep CI_ | sort
  rules:
    - if: $CI_COMMIT_MESSAGE =~ /\[debug\]/
```

### よくある問題と解決法

1. **Docker-in-Docker の問題**
   ```yaml
   # 解決法：特権モードまたはTLS無効化
   services:
     - docker:20.10.16-dind
   variables:
     DOCKER_TLS_CERTDIR: ""  # TLS無効化
   ```

2. **キャッシュの問題**
   ```yaml
   # キャッシュクリア
   clear_cache:
     stage: build
     script:
       - echo "Clearing cache"
     cache: {}
   ```

3. **権限エラー**
   ```yaml
   # 実行権限の設定
   before_script:
     - chmod +x ./deploy.sh
   ```

## パフォーマンス測定

### パイプライン実行時間の最適化

```yaml
# 実行時間測定
.measure_time: &measure_time
  before_script:
    - export START_TIME=$(date +%s)
  after_script:
    - export END_TIME=$(date +%s)
    - echo "Job execution time: $((END_TIME - START_TIME)) seconds"

build:
  <<: *measure_time
  script:
    - npm ci
    - npm run build
```

これらのベストプラクティスを参考に、プロジェクトに最適なCI/CDパイプラインを構築してください。

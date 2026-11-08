# 実践的なサンプル - Node.jsアプリケーション

## プロジェクト構成例

```
my-app/
├── .gitlab-ci.yml          # CI/CD設定
├── package.json            # 依存関係
├── Dockerfile             # Dockerイメージ定義
├── docker-compose.yml     # 開発環境用
├── src/                   # ソースコード
│   ├── app.js
│   └── routes/
├── tests/                 # テスト
│   ├── unit/
│   └── integration/
└── scripts/              # デプロイスクリプト
    └── deploy.sh
```

## .gitlab-ci.yml（完全版）

```yaml
stages:
  - build
  - test
  - security
  - package
  - deploy
  - notify

variables:
  NODE_VERSION: "18"
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: "/certs"
  
# キャッシュ設定
.node_cache: &node_cache
  cache:
    key:
      files:
        - package-lock.json
    paths:
      - node_modules/
    policy: pull-push

# アンカー定義（共通設定）
.node_template: &node_template
  image: node:$NODE_VERSION
  <<: *node_cache

.deploy_template: &deploy_template
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client rsync
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh

# ビルド
build:
  <<: *node_template
  stage: build
  script:
    - npm ci
    - npm run build
    - npm run lint
  artifacts:
    paths:
      - dist/
      - node_modules/
    expire_in: 1 hour
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# ユニットテスト
test:unit:
  <<: *node_template
  stage: test
  dependencies:
    - build
  script:
    - npm run test:unit
  coverage: '/Coverage: \d+\.\d+%/'
  artifacts:
    reports:
      junit: junit.xml
      coverage_report:
        coverage_format: cobertura
        path: coverage/cobertura-coverage.xml
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# 結合テスト
test:integration:
  <<: *node_template
  stage: test
  services:
    - postgres:13
    - redis:6
  variables:
    POSTGRES_DB: test
    POSTGRES_USER: test
    POSTGRES_PASSWORD: test
    DATABASE_URL: "postgresql://test:test@postgres:5432/test"
    REDIS_URL: "redis://redis:6379"
  dependencies:
    - build
  script:
    - npm run test:integration
  artifacts:
    reports:
      junit: integration-junit.xml
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# セキュリティスキャン
security:sast:
  stage: security
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  script:
    - docker run --rm -v "$PWD:/app" -w /app returntocorp/semgrep --config=auto --json --output=sast-report.json
  artifacts:
    reports:
      sast: sast-report.json
  allow_failure: true
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# 依存関係脆弱性スキャン
security:dependency:
  <<: *node_template
  stage: security
  script:
    - npm audit --audit-level=high
  allow_failure: true
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# Dockerイメージビルド
package:docker:
  stage: package
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  dependencies:
    - build
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA -t $CI_REGISTRY_IMAGE:latest .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - docker push $CI_REGISTRY_IMAGE:latest
    # 脆弱性スキャン
    - docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# ステージング環境デプロイ
deploy:staging:
  <<: *deploy_template
  stage: deploy
  dependencies:
    - package:docker
  script:
    - echo "$SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
    - ssh $SSH_USER@$STAGING_SERVER "docker pull $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA"
    - ssh $SSH_USER@$STAGING_SERVER "docker stop myapp-staging || true"
    - ssh $SSH_USER@$STAGING_SERVER "docker rm myapp-staging || true"
    - ssh $SSH_USER@$STAGING_SERVER "docker run -d --name myapp-staging -p 3001:3000 -e NODE_ENV=staging $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA"
    # ヘルスチェック
    - sleep 10
    - curl -f http://$STAGING_SERVER:3001/health
  environment:
    name: staging
    url: http://$STAGING_SERVER:3001
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"

# 本番環境デプロイ
deploy:production:
  <<: *deploy_template
  stage: deploy
  dependencies:
    - package:docker
  script:
    - echo "$SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
    # 現在のコンテナをバックアップとして保持
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker tag myapp-prod myapp-prod-backup || true"
    # 新しいイメージをプル
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker pull $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA"
    # ゼロダウンタイムデプロイ
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker run -d --name myapp-prod-new -p 3001:3000 -e NODE_ENV=production $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA"
    # ヘルスチェック
    - sleep 15
    - curl -f http://$PRODUCTION_SERVER:3001/health
    # トラフィック切り替え（ロードバランサー設定変更等）
    - ssh $SSH_USER@$PRODUCTION_SERVER "sudo nginx -s reload"
    # 古いコンテナを停止
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker stop myapp-prod || true"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker rm myapp-prod || true"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker rename myapp-prod-new myapp-prod"
  after_script:
    # デプロイ失敗時のロールバック
    - |
      if [ $CI_JOB_STATUS == 'failed' ]; then
        echo "Deployment failed, rolling back..."
        ssh $SSH_USER@$PRODUCTION_SERVER "docker stop myapp-prod-new || true"
        ssh $SSH_USER@$PRODUCTION_SERVER "docker rm myapp-prod-new || true"
        ssh $SSH_USER@$PRODUCTION_SERVER "docker run -d --name myapp-prod -p 3000:3000 -e NODE_ENV=production myapp-prod-backup"
      fi
  environment:
    name: production
    url: https://example.com
  when: manual
  rules:
    - if: $CI_COMMIT_BRANCH == "main"

# 成功通知
notify:success:
  stage: notify
  image: alpine:latest
  dependencies: []
  before_script:
    - apk add --no-cache curl
  script:
    - |
      curl -X POST -H 'Content-type: application/json' \
        --data "{
          \"text\": \"✅ Deployment successful!\",
          \"attachments\": [{
            \"color\": \"good\",
            \"fields\": [
              {\"title\": \"Project\", \"value\": \"$CI_PROJECT_NAME\", \"short\": true},
              {\"title\": \"Branch\", \"value\": \"$CI_COMMIT_REF_NAME\", \"short\": true},
              {\"title\": \"Commit\", \"value\": \"$CI_COMMIT_SHORT_SHA\", \"short\": true},
              {\"title\": \"Pipeline\", \"value\": \"$CI_PIPELINE_URL\", \"short\": false}
            ]
          }]
        }" \
        $SLACK_WEBHOOK_URL
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  when: on_success

# 失敗通知
notify:failure:
  stage: notify
  image: alpine:latest
  dependencies: []
  before_script:
    - apk add --no-cache curl
  script:
    - |
      curl -X POST -H 'Content-type: application/json' \
        --data "{
          \"text\": \"❌ Deployment failed!\",
          \"attachments\": [{
            \"color\": \"danger\",
            \"fields\": [
              {\"title\": \"Project\", \"value\": \"$CI_PROJECT_NAME\", \"short\": true},
              {\"title\": \"Branch\", \"value\": \"$CI_COMMIT_REF_NAME\", \"short\": true},
              {\"title\": \"Failed Job\", \"value\": \"$CI_JOB_NAME\", \"short\": true},
              {\"title\": \"Pipeline\", \"value\": \"$CI_PIPELINE_URL\", \"short\": false}
            ]
          }]
        }" \
        $SLACK_WEBHOOK_URL
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  when: on_failure
```

## package.json例

```json
{
  "name": "my-app",
  "version": "1.0.0",
  "scripts": {
    "start": "node src/app.js",
    "dev": "nodemon src/app.js",
    "build": "webpack --mode=production",
    "test": "npm run test:unit && npm run test:integration",
    "test:unit": "jest --testPathPattern=tests/unit --coverage --coverageReporters=text --coverageReporters=cobertura",
    "test:integration": "jest --testPathPattern=tests/integration",
    "lint": "eslint src/ tests/",
    "lint:fix": "eslint src/ tests/ --fix"
  },
  "dependencies": {
    "express": "^4.18.0",
    "helmet": "^6.1.0",
    "cors": "^2.8.5"
  },
  "devDependencies": {
    "jest": "^29.0.0",
    "nodemon": "^2.0.0",
    "eslint": "^8.0.0",
    "supertest": "^6.3.0"
  }
}
```

## Dockerfile例

```dockerfile
# マルチステージビルド
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production && npm cache clean --force

FROM node:18-alpine AS production

# セキュリティ: 非rootユーザーで実行
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001

WORKDIR /app

# 依存関係をコピー
COPY --from=builder /app/node_modules ./node_modules
COPY --chown=nextjs:nodejs package*.json ./
COPY --chown=nextjs:nodejs src ./src

# セキュリティヘルスチェック
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3000/health || exit 1

USER nextjs

EXPOSE 3000

CMD ["npm", "start"]
```

## 必要な環境変数

GitLabプロジェクトのSettings → CI/CD → Variablesで設定：

| 変数名              | 値の例                                   | 保護 | マスク |
| ------------------- | ---------------------------------------- | ---- | ------ |
| `SSH_PRIVATE_KEY`   | `-----BEGIN OPENSSH PRIVATE KEY-----...` | ✓    | ✓      |
| `SSH_KNOWN_HOSTS`   | `example.com ssh-rsa AAAAB3N...`         | -    | -      |
| `SSH_USER`          | `deploy`                                 | -    | -      |
| `STAGING_SERVER`    | `staging.example.com`                    | -    | -      |
| `PRODUCTION_SERVER` | `production.example.com`                 | ✓    | -      |
| `SLACK_WEBHOOK_URL` | `https://hooks.slack.com/services/...`   | ✓    | ✓      |

## デプロイスクリプト例（scripts/deploy.sh）

```bash
#!/bin/bash

set -e

ENVIRONMENT=${1:-staging}
IMAGE_TAG=${2:-latest}

echo "Deploying to $ENVIRONMENT with image tag: $IMAGE_TAG"

case $ENVIRONMENT in
  staging)
    SERVER=$STAGING_SERVER
    PORT=3001
    ;;
  production)
    SERVER=$PRODUCTION_SERVER
    PORT=3000
    ;;
  *)
    echo "Unknown environment: $ENVIRONMENT"
    exit 1
    ;;
esac

echo "Connecting to $SERVER..."

# 新しいコンテナを起動
ssh $SSH_USER@$SERVER "docker pull $CI_REGISTRY_IMAGE:$IMAGE_TAG"
ssh $SSH_USER@$SERVER "docker run -d --name myapp-$ENVIRONMENT-new -p $((PORT+1)):3000 -e NODE_ENV=$ENVIRONMENT $CI_REGISTRY_IMAGE:$IMAGE_TAG"

# ヘルスチェック
echo "Waiting for application to be ready..."
for i in {1..30}; do
    if ssh $SSH_USER@$SERVER "curl -f http://localhost:$((PORT+1))/health"; then
        echo "Application is ready!"
        break
    fi
    echo "Waiting... ($i/30)"
    sleep 5
done

# トラフィック切り替え
echo "Switching traffic..."
ssh $SSH_USER@$SERVER "docker stop myapp-$ENVIRONMENT || true"
ssh $SSH_USER@$SERVER "docker rm myapp-$ENVIRONMENT || true"
ssh $SSH_USER@$SERVER "docker rename myapp-$ENVIRONMENT-new myapp-$ENVIRONMENT"
ssh $SSH_USER@$SERVER "docker port myapp-$ENVIRONMENT"

echo "Deployment completed successfully!"
```

## 使用開始手順

1. **リポジトリの準備**
   ```bash
   git clone <your-repo>
   cd <your-repo>
   cp .gitlab-ci.yml.example .gitlab-ci.yml
   ```

2. **環境変数の設定**
   - GitLabプロジェクトの設定で必要な変数を設定

3. **サーバーの準備**
   ```bash
   # デプロイ用ユーザー作成
   sudo useradd -m -s /bin/bash deploy
   sudo usermod -aG docker deploy
   
   # SSH鍵設定
   sudo -u deploy mkdir -p /home/deploy/.ssh
   sudo -u deploy chmod 700 /home/deploy/.ssh
   echo "your-public-key" | sudo -u deploy tee /home/deploy/.ssh/authorized_keys
   sudo -u deploy chmod 600 /home/deploy/.ssh/authorized_keys
   ```

4. **最初のデプロイ**
   ```bash
   git add .
   git commit -m "Add CI/CD configuration"
   git push origin main
   ```

このサンプルを基に、あなたのプロジェクトに合わせてカスタマイズしてください！

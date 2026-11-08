# Docker デプロイ例

## 概要
DockerイメージをビルドしてContainer Registryにプッシュし、本番環境にデプロイする例を説明します。

## 基本的なDockerデプロイパイプライン

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - publish
  - deploy

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: "/certs"
  IMAGE_TAG: $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  LATEST_TAG: $CI_REGISTRY_IMAGE:latest

# サービス（Docker-in-Docker）
services:
  - docker:20.10.16-dind

before_script:
  - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY

# アプリケーションビルド
build_app:
  stage: build
  image: node:18
  script:
    - npm ci
    - npm run build
  artifacts:
    paths:
      - dist/
    expire_in: 1 hour

# テスト実行
test_app:
  stage: test
  image: node:18
  script:
    - npm ci
    - npm run test:unit
    - npm run test:integration
  coverage: '/Coverage: \d+\.\d+%/'

# Dockerイメージビルド・プッシュ
build_docker_image:
  stage: publish
  image: docker:20.10.16
  dependencies:
    - build_app
  script:
    - docker build -t $IMAGE_TAG -t $LATEST_TAG .
    - docker push $IMAGE_TAG
    - docker push $LATEST_TAG
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# ステージング環境デプロイ
deploy_staging:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - echo "$SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
    - chmod 644 ~/.ssh/known_hosts
  script:
    - ssh $SSH_USER@$STAGING_SERVER "docker pull $IMAGE_TAG"
    - ssh $SSH_USER@$STAGING_SERVER "docker stop myapp || true"
    - ssh $SSH_USER@$STAGING_SERVER "docker rm myapp || true"
    - ssh $SSH_USER@$STAGING_SERVER "docker run -d --name myapp -p 80:3000 $IMAGE_TAG"
  environment:
    name: staging
    url: https://staging.example.com
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"

# 本番環境デプロイ
deploy_production:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - echo "$SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
    - chmod 644 ~/.ssh/known_hosts
  script:
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker pull $IMAGE_TAG"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker stop myapp || true"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker rm myapp || true"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker run -d --name myapp -p 80:3000 $IMAGE_TAG"
  environment:
    name: production
    url: https://example.com
  when: manual
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH
```

## Docker Composeを使用したデプロイ

```yaml
# docker-compose版のデプロイ
deploy_with_compose:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client docker-compose
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
  script:
    # docker-compose.ymlをサーバーに転送
    - scp docker-compose.prod.yml $SSH_USER@$PRODUCTION_SERVER:/opt/myapp/
    # 環境変数ファイルを作成
    - ssh $SSH_USER@$PRODUCTION_SERVER "echo 'IMAGE_TAG=$IMAGE_TAG' > /opt/myapp/.env"
    # サービス更新
    - ssh $SSH_USER@$PRODUCTION_SERVER "cd /opt/myapp && docker-compose -f docker-compose.prod.yml pull"
    - ssh $SSH_USER@$PRODUCTION_SERVER "cd /opt/myapp && docker-compose -f docker-compose.prod.yml up -d"
  environment:
    name: production
```

## 必要なDockerfile例

```dockerfile
# Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

FROM node:18-alpine AS production

WORKDIR /app
COPY --from=builder /app/node_modules ./node_modules
COPY dist ./dist
COPY package*.json ./

EXPOSE 3000

USER node

CMD ["npm", "start"]
```

## docker-compose.prod.yml例

```yaml
version: '3.8'

services:
  app:
    image: ${IMAGE_TAG}
    ports:
      - "80:3000"
    environment:
      - NODE_ENV=production
    restart: unless-stopped
    networks:
      - app-network

  nginx:
    image: nginx:alpine
    ports:
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/ssl/certs
    depends_on:
      - app
    restart: unless-stopped
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

## 必要な環境変数設定

GitLabのSettings → CI/CD → Variablesで以下を設定：

| 変数名              | 説明                            | 保護 |
| ------------------- | ------------------------------- | ---- |
| `SSH_PRIVATE_KEY`   | デプロイ用秘密鍵                | ✓    |
| `SSH_KNOWN_HOSTS`   | 接続先サーバーのホストキー      | -    |
| `SSH_USER`          | SSH接続ユーザー                 | -    |
| `STAGING_SERVER`    | ステージングサーバーIP/ホスト名 | -    |
| `PRODUCTION_SERVER` | 本番サーバーIP/ホスト名         | ✓    |

## セキュリティのベストプラクティス

1. **専用ユーザーの作成**
   ```bash
   # サーバー側で実行
   sudo useradd -m -s /bin/bash deploy
   sudo usermod -aG docker deploy
   ```

2. **SSH鍵の生成**
   ```bash
   ssh-keygen -t ed25519 -C "gitlab-ci-deploy"
   # 公開鍵をサーバーの ~/.ssh/authorized_keys に追加
   ```

3. **Dockerイメージの脆弱性スキャン**
   ```yaml
   security_scan:
     stage: test
     image: docker:20.10.16
     script:
       - docker run --rm -v /var/run/docker.sock:/var/run/docker.sock 
         aquasec/trivy image $IMAGE_TAG
   ```

## トラブルシューティング

### よくある問題と解決法

1. **Docker-in-Docker接続エラー**
   - `DOCKER_TLS_CERTDIR: ""`を追加
   - 特権モードが必要な場合は`privileged: true`

2. **イメージプッシュエラー**
   - レジストリログインの確認
   - 権限設定の確認

3. **SSH接続エラー**
   - ホストキーの確認
   - 秘密鍵のフォーマット確認（改行コードなど）

## モニタリング

```yaml
# ヘルスチェック付きデプロイ
deploy_with_healthcheck:
  stage: deploy
  script:
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker pull $IMAGE_TAG"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker stop myapp || true"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker rm myapp || true"
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker run -d --name myapp -p 80:3000 --health-cmd='curl -f http://localhost:3000/health' --health-interval=30s $IMAGE_TAG"
    # ヘルスチェック待機
    - sleep 60
    - ssh $SSH_USER@$PRODUCTION_SERVER "docker inspect --format='{{.State.Health.Status}}' myapp | grep healthy"
  after_script:
    # デプロイ失敗時のロールバック
    - |
      if [ $CI_JOB_STATUS == 'failed' ]; then
        ssh $SSH_USER@$PRODUCTION_SERVER "docker stop myapp || true"
        ssh $SSH_USER@$PRODUCTION_SERVER "docker rm myapp || true"
        ssh $SSH_USER@$PRODUCTION_SERVER "docker run -d --name myapp -p 80:3000 $PREVIOUS_IMAGE_TAG"
      fi
```

このDockerデプロイ例を参考に、あなたのプロジェクトに合わせてカスタマイズしてください。

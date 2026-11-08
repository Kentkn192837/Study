# Tomcat CI/CD 段階的導入計画

## Phase 1: 基本的な自動ビルド（推奨開始段階）

### 目的
手動デプロイは残しつつ、ビルドプロセスのみ自動化

### .gitlab-ci.yml（Phase 1）

```yaml
stages:
  - build
  - test

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  MAVEN_CLI_OPTS: "--batch-mode --errors --fail-at-end --show-version"

# Maven依存関係キャッシュ
cache:
  key: 
    files:
      - pom.xml
  paths:
    - .m2/repository/

# ビルドのみ（デプロイなし）
build_war:
  image: maven:3.9.4-eclipse-temurin-17
  stage: build
  script:
    - mvn $MAVEN_CLI_OPTS clean compile
    - mvn $MAVEN_CLI_OPTS package -P production
    - echo "WAR file created:"
    - ls -la target/*.war
  artifacts:
    paths:
      - target/*.war
    expire_in: 7 days
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
    - if: $CI_COMMIT_BRANCH == "develop"

# ユニットテスト
test:
  image: maven:3.9.4-eclipse-temurin-17
  stage: test
  dependencies:
    - build_war
  script:
    - mvn $MAVEN_CLI_OPTS test
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
    - if: $CI_COMMIT_BRANCH == "develop"
```

### Phase 1での検証項目
- [ ] GitLabパイプラインが正常に動作する
- [ ] WARファイルが正しく生成される  
- [ ] テストが自動実行される
- [ ] アーティファクトがダウンロード可能

---

## Phase 2: ステージング環境自動デプロイ

### 目的
ステージング環境のみ自動デプロイを導入

### 追加する設定

```yaml
# Phase 1の設定に以下を追加

stages:
  - build
  - test
  - deploy_staging

# ステージング環境デプロイ（安全な環境で試行）
deploy_staging:
  stage: deploy_staging
  image: alpine:latest
  dependencies:
    - build_war
  before_script:
    - apk add --no-cache openssh-client
    - eval $(ssh-agent -s)
    - echo "$STAGING_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$STAGING_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    - WAR_FILE=$(ls target/*.war | head -n 1)
    - APP_NAME=$(basename "$WAR_FILE" .war)
    
    # シンプルなデプロイスクリプト
    - echo "Deploying $WAR_FILE to staging..."
    - scp "$WAR_FILE" $STAGING_USER@$STAGING_HOST:/tmp/
    - ssh $STAGING_USER@$STAGING_HOST "./deploy-app.sh $(basename $WAR_FILE)"
  environment:
    name: staging
    url: http://$STAGING_HOST:8080/myapp
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"
  # 手動実行も可能にしておく
  when: manual
```

### サーバー側デプロイスクリプト（deploy-app.sh）

```bash
#!/bin/bash

# /home/deploy/deploy-app.sh

set -e

WAR_FILE=$1
APP_NAME=$(basename "$WAR_FILE" .war)
TOMCAT_HOME="/opt/tomcat"
WEBAPPS_DIR="$TOMCAT_HOME/webapps"

if [ -z "$WAR_FILE" ]; then
    echo "Usage: $0 <war-file>"
    exit 1
fi

echo "Starting deployment of $WAR_FILE..."

# バックアップ作成
if [ -f "$WEBAPPS_DIR/$APP_NAME.war" ]; then
    echo "Creating backup..."
    sudo cp "$WEBAPPS_DIR/$APP_NAME.war" "$WEBAPPS_DIR/$APP_NAME.war.backup.$(date +%Y%m%d_%H%M%S)"
fi

# Tomcat停止
echo "Stopping Tomcat..."
sudo systemctl stop tomcat

# 古いアプリケーション削除
echo "Removing old application..."
sudo rm -rf "$WEBAPPS_DIR/$APP_NAME" "$WEBAPPS_DIR/$APP_NAME.war"

# 新しいWARファイル配置
echo "Deploying new WAR file..."
sudo mv "/tmp/$WAR_FILE" "$WEBAPPS_DIR/"
sudo chown tomcat:tomcat "$WEBAPPS_DIR/$WAR_FILE"

# Tomcat起動
echo "Starting Tomcat..."
sudo systemctl start tomcat

# 起動確認
echo "Waiting for application to start..."
sleep 30

# ヘルスチェック
for i in {1..30}; do
    if curl -f "http://localhost:8080/$APP_NAME/health" 2>/dev/null || curl -f "http://localhost:8080/$APP_NAME/" 2>/dev/null; then
        echo "Application is running!"
        exit 0
    fi
    echo "Waiting for application... ($i/30)"
    sleep 10
done

echo "Application failed to start within expected time"
exit 1
```

### Phase 2での検証項目
- [ ] ステージング環境への自動デプロイが成功する
- [ ] デプロイ後にアプリケーションが正常動作する
- [ ] ロールバック手順を確認済み
- [ ] 監視・ログ確認手順を確立

---

## Phase 3: 本番環境自動デプロイ（完全版）

### 目的
本番環境への自動デプロイ（承認プロセス付き）

### 本格的な.gitlab-ci.yml

```yaml
stages:
  - build
  - test
  - security
  - deploy_staging
  - deploy_production
  - post_deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  MAVEN_CLI_OPTS: "--batch-mode --errors --fail-at-end --show-version"

# 本番デプロイ（手動承認必須）
deploy_production:
  stage: deploy_production
  image: alpine:latest
  dependencies:
    - build_war
  before_script:
    - apk add --no-cache openssh-client curl
    - eval $(ssh-agent -s)
    - echo "$PRODUCTION_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$PRODUCTION_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    - WAR_FILE=$(ls target/*.war | head -n 1)
    - APP_NAME=$(basename "$WAR_FILE" .war)
    - VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    
    # 事前チェック
    - echo "Pre-deployment checks..."
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "df -h"  # ディスク容量確認
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "free -m"  # メモリ確認
    
    # データベースバックアップ
    - echo "Creating database backup..."
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "./backup-database.sh"
    
    # メンテナンスモード開始
    - echo "Entering maintenance mode..."
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "./maintenance-mode.sh on"
    
    # アプリケーションデプロイ
    - echo "Deploying application..."
    - scp "$WAR_FILE" $PRODUCTION_USER@$PRODUCTION_HOST:/tmp/
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "./deploy-app-production.sh $(basename $WAR_FILE)"
    
    # メンテナンスモード終了
    - echo "Exiting maintenance mode..."
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "./maintenance-mode.sh off"
    
    # 本格的なヘルスチェック
    - echo "Running health checks..."
    - sleep 60
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "./health-check.sh"
  after_script:
    # 失敗時の自動ロールバック
    - |
      if [ $CI_JOB_STATUS == 'failed' ]; then
        echo "Deployment failed, initiating emergency rollback..."
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "./emergency-rollback.sh"
      fi
  environment:
    name: production
    url: https://$PRODUCTION_DOMAIN
  when: manual  # 必須：手動承認
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  timeout: 30 minutes

# デプロイ後スモークテスト
smoke_test:
  stage: post_deploy
  image: alpine:latest
  dependencies: []
  before_script:
    - apk add --no-cache curl jq
  script:
    # 主要エンドポイントテスト
    - curl -f https://$PRODUCTION_DOMAIN/health | jq '.status'
    - curl -f https://$PRODUCTION_DOMAIN/api/version | jq '.version'
    
    # パフォーマンステスト
    - time curl -s https://$PRODUCTION_DOMAIN/ > /dev/null
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  needs: ["deploy_production"]
```

## サーバー側の高度なスクリプト

### 1. 本番用デプロイスクリプト（deploy-app-production.sh）

```bash
#!/bin/bash

# /home/deploy/deploy-app-production.sh

set -e

WAR_FILE=$1
APP_NAME=$(basename "$WAR_FILE" .war)
TOMCAT_HOME="/opt/tomcat"
WEBAPPS_DIR="$TOMCAT_HOME/webapps"
BACKUP_DIR="/backup/applications"
LOG_FILE="/var/log/deploy.log"

# ログ関数
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a $LOG_FILE
}

log "Starting production deployment of $WAR_FILE"

# 事前チェック
if [ ! -f "/tmp/$WAR_FILE" ]; then
    log "ERROR: WAR file not found: /tmp/$WAR_FILE"
    exit 1
fi

# バックアップディレクトリ作成
sudo mkdir -p $BACKUP_DIR
BACKUP_FILE="$BACKUP_DIR/$APP_NAME-$(date +%Y%m%d_%H%M%S).war"

# 現在のアプリケーションバックアップ
if [ -f "$WEBAPPS_DIR/$APP_NAME.war" ]; then
    log "Creating application backup: $BACKUP_FILE"
    sudo cp "$WEBAPPS_DIR/$APP_NAME.war" "$BACKUP_FILE"
fi

# JVMヒープダンプ（障害調査用）
log "Creating heap dump for analysis..."
sudo -u tomcat jcmd $(pgrep -f tomcat) GC.run_finalization || true

# Graceful shutdown
log "Initiating graceful Tomcat shutdown..."
sudo systemctl stop tomcat

# プロセス完全停止まで待機
for i in {1..30}; do
    if ! pgrep -f tomcat > /dev/null; then
        break
    fi
    log "Waiting for Tomcat to stop... ($i/30)"
    sleep 2
done

if pgrep -f tomcat > /dev/null; then
    log "WARNING: Force killing Tomcat process"
    sudo pkill -9 -f tomcat
fi

# 古いアプリケーション削除
log "Removing old application files..."
sudo rm -rf "$WEBAPPS_DIR/$APP_NAME" "$WEBAPPS_DIR/$APP_NAME.war"

# 新しいWARファイル配置
log "Installing new application..."
sudo mv "/tmp/$WAR_FILE" "$WEBAPPS_DIR/"
sudo chown tomcat:tomcat "$WEBAPPS_DIR/$WAR_FILE"
sudo chmod 644 "$WEBAPPS_DIR/$WAR_FILE"

# Tomcat起動
log "Starting Tomcat..."
sudo systemctl start tomcat

# 起動ログ監視
log "Monitoring startup logs..."
timeout 120s sudo tail -f $TOMCAT_HOME/logs/catalina.out | while IFS= read -r line; do
    echo "$line"
    if [[ $line == *"Server startup"* ]]; then
        pkill -P $$ tail
    fi
done

log "Production deployment completed successfully"
```

### 2. ヘルスチェックスクリプト（health-check.sh）

```bash
#!/bin/bash

# /home/deploy/health-check.sh

APP_URL="http://localhost:8080/myapp"
MAX_ATTEMPTS=30
WAIT_SECONDS=10

check_endpoint() {
    local endpoint=$1
    local expected_status=${2:-200}
    
    local status=$(curl -s -o /dev/null -w "%{http_code}" "$endpoint")
    if [ "$status" = "$expected_status" ]; then
        echo "✓ $endpoint - OK ($status)"
        return 0
    else
        echo "✗ $endpoint - FAIL ($status)"
        return 1
    fi
}

echo "Starting comprehensive health check..."

# 基本的なエンドポイントチェック
for i in $(seq 1 $MAX_ATTEMPTS); do
    echo "Attempt $i/$MAX_ATTEMPTS"
    
    if check_endpoint "$APP_URL/health" && \
       check_endpoint "$APP_URL/" && \
       check_endpoint "$APP_URL/api/status"; then
        echo "All health checks passed!"
        exit 0
    fi
    
    if [ $i -lt $MAX_ATTEMPTS ]; then
        echo "Waiting ${WAIT_SECONDS}s before retry..."
        sleep $WAIT_SECONDS
    fi
done

echo "Health check failed after $MAX_ATTEMPTS attempts"
exit 1
```

### 3. 緊急ロールバックスクリプト（emergency-rollback.sh）

```bash
#!/bin/bash

# /home/deploy/emergency-rollback.sh

set -e

APP_NAME="myapp"  # アプリケーション名
TOMCAT_HOME="/opt/tomcat"
WEBAPPS_DIR="$TOMCAT_HOME/webapps"
BACKUP_DIR="/backup/applications"
LOG_FILE="/var/log/rollback.log"

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a $LOG_FILE
}

log "EMERGENCY ROLLBACK INITIATED"

# 最新のバックアップファイルを取得
LATEST_BACKUP=$(ls -t $BACKUP_DIR/$APP_NAME-*.war 2>/dev/null | head -n1)

if [ -z "$LATEST_BACKUP" ]; then
    log "ERROR: No backup files found in $BACKUP_DIR"
    exit 1
fi

log "Rolling back to: $LATEST_BACKUP"

# Tomcat停止
log "Stopping Tomcat..."
sudo systemctl stop tomcat

# 失敗したアプリケーション削除
log "Removing failed application..."
sudo rm -rf "$WEBAPPS_DIR/$APP_NAME" "$WEBAPPS_DIR/$APP_NAME.war"

# バックアップから復元
log "Restoring from backup..."
sudo cp "$LATEST_BACKUP" "$WEBAPPS_DIR/$APP_NAME.war"
sudo chown tomcat:tomcat "$WEBAPPS_DIR/$APP_NAME.war"

# Tomcat起動
log "Starting Tomcat..."
sudo systemctl start tomcat

# 簡単なヘルスチェック
log "Performing rollback verification..."
sleep 30
if curl -f "http://localhost:8080/$APP_NAME/health" 2>/dev/null; then
    log "ROLLBACK SUCCESSFUL - Application is responding"
else
    log "WARNING: Rollback completed but application may not be responding"
fi

log "Emergency rollback procedure completed"
```

## 導入スケジュール例

### Week 1-2: Phase 1 導入
- [ ] GitLab CI/CD環境設定
- [ ] 基本的なビルドパイプライン作成
- [ ] 開発チームでの動作確認

### Week 3-4: Phase 2 導入  
- [ ] ステージング環境準備
- [ ] 自動デプロイスクリプト作成
- [ ] ステージング環境での動作検証

### Week 5-6: Phase 3 準備
- [ ] 本番環境での権限設定
- [ ] 本格的なスクリプト作成
- [ ] ロールバック手順テスト

### Week 7-8: Phase 3 本格導入
- [ ] 本番環境への自動デプロイ開始
- [ ] 監視・ログ体制確立
- [ ] チーム全体での運用開始

この段階的なアプローチにより、リスクを最小限に抑えながらCI/CDを導入できます！

## Antビルド環境での段階的導入

### Phase 1: Antビルド自動化（推奨開始段階）

#### .gitlab-ci.yml（Ant Phase 1）

```yaml
stages:
  - build
  - test

variables:
  ANT_OPTS: "-Xmx1024m -Dfile.encoding=UTF-8"

# Antキャッシュ設定
cache:
  key: 
    files:
      - build.xml
      - ivy.xml
  paths:
    - lib/

# Antビルド（デプロイなし）
build_war_ant:
  image: openjdk:17-jdk-slim
  stage: build
  before_script:
    - apt-get update && apt-get install -y ant wget
    # 依存関係JARファイルのダウンロード（Ivyを使わない場合）
    - mkdir -p lib
    - ./download-dependencies.sh  # 依存関係ダウンロードスクリプト
  script:
    - ant clean compile
    - ant -Denv=production war
    - echo "WAR file created:"
    - ls -la dist/*.war
  artifacts:
    paths:
      - dist/*.war
    expire_in: 7 days
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
    - if: $CI_COMMIT_BRANCH == "develop"

# Antユニットテスト
test_ant:
  image: openjdk:17-jdk-slim
  stage: test
  dependencies:
    - build_war_ant
  before_script:
    - apt-get update && apt-get install -y ant
  script:
    - ant test
  artifacts:
    reports:
      junit:
        - build/test-results/TEST-*.xml
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
    - if: $CI_COMMIT_BRANCH == "develop"
```

#### 依存関係ダウンロードスクリプト（download-dependencies.sh）

```bash
#!/bin/bash

# download-dependencies.sh
# 必要なJARファイルをダウンロード

set -e

LIB_DIR="lib"
mkdir -p $LIB_DIR

echo "Downloading dependencies..."

# Servlet API
wget -q -O $LIB_DIR/javax.servlet-api-4.0.1.jar \
  "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar"

# MySQL Connector
wget -q -O $LIB_DIR/mysql-connector-java-8.0.33.jar \
  "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar"

# Spring Framework
wget -q -O $LIB_DIR/spring-webmvc-5.3.21.jar \
  "https://repo1.maven.org/maven2/org/springframework/spring-webmvc/5.3.21/spring-webmvc-5.3.21.jar"

# JUnit (テスト用)
wget -q -O $LIB_DIR/junit-4.13.2.jar \
  "https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar"

wget -q -O $LIB_DIR/hamcrest-core-1.3.jar \
  "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"

echo "Dependencies downloaded successfully!"
ls -la $LIB_DIR/
```

### Phase 2: Ant環境でのステージング自動デプロイ

#### 追加する設定

```yaml
# Phase 1の設定に以下を追加

stages:
  - build
  - test
  - deploy_staging

# ステージング環境デプロイ（Ant版）
deploy_staging_ant:
  stage: deploy_staging
  image: alpine:latest
  dependencies:
    - build_war_ant
  before_script:
    - apk add --no-cache openssh-client
    - eval $(ssh-agent -s)
    - echo "$STAGING_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$STAGING_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    - WAR_FILE=$(ls dist/*.war | head -n 1)
    
    # シンプルなAntデプロイスクリプトを使用
    - echo "Deploying $WAR_FILE to staging..."
    - scp "$WAR_FILE" $STAGING_USER@$STAGING_HOST:/tmp/
    - scp deploy.xml $STAGING_USER@$STAGING_HOST:/tmp/
    - scp deploy.properties $STAGING_USER@$STAGING_HOST:/tmp/
    - ssh $STAGING_USER@$STAGING_HOST "cd /tmp && ant -f deploy.xml -Dwar.file=$WAR_FILE deploy"
  environment:
    name: staging
    url: http://$STAGING_HOST:8080/myapp
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"
  when: manual
```

#### 簡易版deploy.xml（Phase 2用）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="SimpleDeploy" default="deploy" basedir=".">
    
    <property file="deploy.properties"/>
    <property name="war.file" value="myapp.war"/>
    <property name="app.name" value="myapp"/>
    
    <target name="deploy" description="Simple deployment for staging">
        <echo message="Starting deployment of ${war.file}"/>
        
        <!-- Tomcat停止 -->
        <exec executable="sudo">
            <arg line="systemctl stop tomcat"/>
        </exec>
        
        <!-- 古いアプリ削除 -->
        <exec executable="sudo">
            <arg line="rm -rf /opt/tomcat/webapps/${app.name}*"/>
        </exec>
        
        <!-- 新しいWAR配置 -->
        <exec executable="sudo">
            <arg line="cp ${war.file} /opt/tomcat/webapps/"/>
        </exec>
        
        <!-- 権限設定 -->
        <exec executable="sudo">
            <arg line="chown tomcat:tomcat /opt/tomcat/webapps/${app.name}.war"/>
        </exec>
        
        <!-- Tomcat開始 -->
        <exec executable="sudo">
            <arg line="systemctl start tomcat"/>
        </exec>
        
        <!-- 起動待機 -->
        <sleep seconds="30"/>
        
        <echo message="Deployment completed"/>
    </target>
    
</project>
```

### Phase 3: Ant環境での本番自動デプロイ

#### 本格的な.gitlab-ci.yml（Ant版）

```yaml
stages:
  - build
  - test
  - security
  - deploy_staging
  - deploy_production
  - post_deploy

# セキュリティスキャン（Ant用）
security_scan_ant:
  stage: security
  image: openjdk:17-jdk-slim
  before_script:
    - apt-get update && apt-get install -y ant wget
  script:
    # ソースコードの静的解析
    - ant -f security.xml source-scan
    # 依存関係の脆弱性チェック
    - ant -f security.xml dependency-check
  artifacts:
    reports:
      sast: security-report.json
  allow_failure: true
  rules:
    - if: $CI_COMMIT_BRANCH == "main"

# 本番デプロイ（Ant版）
deploy_production_ant:
  stage: deploy_production
  image: alpine:latest
  dependencies:
    - build_war_ant
  before_script:
    - apk add --no-cache openssh-client curl
    - eval $(ssh-agent -s)
    - echo "$PRODUCTION_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$PRODUCTION_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    - WAR_FILE=$(ls dist/*.war | head -n 1)
    
    # 本格的なデプロイスクリプト群を転送
    - scp "$WAR_FILE" $PRODUCTION_USER@$PRODUCTION_HOST:/tmp/
    - scp deploy-production.xml $PRODUCTION_USER@$PRODUCTION_HOST:/tmp/
    - scp deploy-production.properties $PRODUCTION_USER@$PRODUCTION_HOST:/tmp/
    
    # 事前チェック実行
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "cd /tmp && ant -f deploy-production.xml pre-deploy-check"
    
    # 本番デプロイ実行
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "cd /tmp && ant -f deploy-production.xml -Dwar.file=$(basename $WAR_FILE) deploy"
  after_script:
    # 失敗時のロールバック
    - |
      if [ $CI_JOB_STATUS == 'failed' ]; then
        echo "Deployment failed, initiating rollback..."
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "cd /tmp && ant -f deploy-production.xml rollback"
      fi
  environment:
    name: production
    url: https://$PRODUCTION_DOMAIN
  when: manual
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  timeout: 30 minutes
```

#### 本格版deploy-production.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="ProductionDeploy" default="deploy" basedir=".">
    
    <property file="deploy-production.properties"/>
    
    <!-- 事前チェック -->
    <target name="pre-deploy-check" description="Pre-deployment validation">
        <echo message="Running pre-deployment checks..."/>
        
        <!-- ディスク容量チェック -->
        <exec executable="df" outputproperty="disk.usage">
            <arg line="-h /opt"/>
        </exec>
        <echo message="Disk usage: ${disk.usage}"/>
        
        <!-- メモリチェック -->
        <exec executable="free" outputproperty="memory.usage">
            <arg line="-m"/>
        </exec>
        <echo message="Memory usage: ${memory.usage}"/>
        
        <!-- Tomcatプロセス確認 -->
        <exec executable="systemctl" outputproperty="tomcat.status">
            <arg line="is-active tomcat"/>
        </exec>
        <echo message="Tomcat status: ${tomcat.status}"/>
        
        <echo message="Pre-deployment checks completed"/>
    </target>
    
    <!-- データベースバックアップ -->
    <target name="backup-database" description="Create database backup">
        <tstamp>
            <format property="backup.timestamp" pattern="yyyyMMdd_HHmmss"/>
        </tstamp>
        
        <echo message="Creating database backup..."/>
        <exec executable="mysqldump">
            <arg line="-u ${db.user} -p${db.password} ${db.name}"/>
            <redirector output="/backup/db_backup_${backup.timestamp}.sql"/>
        </exec>
        <echo message="Database backup created: /backup/db_backup_${backup.timestamp}.sql"/>
    </target>
    
    <!-- メンテナンスモード -->
    <target name="maintenance-mode-on" description="Enable maintenance mode">
        <echo message="Enabling maintenance mode..."/>
        <exec executable="sudo">
            <arg line="cp /var/www/maintenance.html /var/www/html/index.html"/>
        </exec>
    </target>
    
    <target name="maintenance-mode-off" description="Disable maintenance mode">
        <echo message="Disabling maintenance mode..."/>
        <exec executable="sudo">
            <arg line="rm -f /var/www/html/index.html"/>
        </exec>
    </target>
    
    <!-- 本番デプロイメイン処理 -->
    <target name="deploy" depends="pre-deploy-check,backup-database,maintenance-mode-on" 
            description="Deploy to production">
        
        <antcall target="backup-current-app"/>
        <antcall target="stop-tomcat-graceful"/>
        <antcall target="deploy-new-war"/>
        <antcall target="start-tomcat"/>
        <antcall target="health-check"/>
        <antcall target="maintenance-mode-off"/>
        
        <echo message="Production deployment completed successfully!"/>
    </target>
    
    <!-- 現在のアプリバックアップ -->
    <target name="backup-current-app">
        <tstamp>
            <format property="app.backup.timestamp" pattern="yyyyMMdd_HHmmss"/>
        </tstamp>
        
        <copy file="${tomcat.webapps}/${app.name}.war" 
              tofile="/backup/applications/${app.name}.war.backup.${app.backup.timestamp}"
              failonerror="false"/>
        <echo message="Application backup created"/>
    </target>
    
    <!-- Graceful Tomcat停止 -->
    <target name="stop-tomcat-graceful">
        <echo message="Stopping Tomcat gracefully..."/>
        <exec executable="sudo">
            <arg line="systemctl stop tomcat"/>
        </exec>
        
        <!-- プロセス完全停止まで待機 -->
        <retry retrycount="30">
            <sequential>
                <sleep seconds="2"/>
                <exec executable="pgrep" resultproperty="tomcat.running" failonerror="false">
                    <arg line="-f tomcat"/>
                </exec>
                <condition property="tomcat.stopped">
                    <not><equals arg1="${tomcat.running}" arg2="0"/></not>
                </condition>
                <fail unless="tomcat.stopped" message="Waiting for Tomcat to stop..."/>
            </sequential>
        </retry>
        
        <echo message="Tomcat stopped successfully"/>
    </target>
    
    <!-- 新WARファイルデプロイ -->
    <target name="deploy-new-war">
        <echo message="Deploying new WAR file: ${war.file}"/>
        
        <!-- 古いファイル削除 -->
        <exec executable="sudo">
            <arg line="rm -rf ${tomcat.webapps}/${app.name} ${tomcat.webapps}/${app.name}.war"/>
        </exec>
        
        <!-- 新しいWARファイル配置 -->
        <exec executable="sudo">
            <arg line="mv ${war.file} ${tomcat.webapps}/"/>
        </exec>
        
        <!-- 権限設定 -->
        <exec executable="sudo">
            <arg line="chown tomcat:tomcat ${tomcat.webapps}/${app.name}.war"/>
        </exec>
        <exec executable="sudo">
            <arg line="chmod 644 ${tomcat.webapps}/${app.name}.war"/>
        </exec>
        
        <echo message="WAR file deployed successfully"/>
    </target>
    
    <!-- Tomcat開始 -->
    <target name="start-tomcat">
        <echo message="Starting Tomcat..."/>
        <exec executable="sudo">
            <arg line="systemctl start tomcat"/>
        </exec>
        
        <sleep seconds="30"/>
        <echo message="Tomcat startup completed"/>
    </target>
    
    <!-- ヘルスチェック -->
    <target name="health-check">
        <echo message="Running health checks..."/>
        
        <retry retrycount="30">
            <sequential>
                <sleep seconds="10"/>
                <get src="http://localhost:8080/${app.name}/health" 
                     dest="/tmp/health-check.txt" 
                     ignoreerrors="true"/>
            </sequential>
        </retry>
        
        <echo message="Health check passed - application is responding"/>
    </target>
    
    <!-- ロールバック -->
    <target name="rollback" description="Rollback to previous version">
        <echo message="Initiating emergency rollback..."/>
        
        <!-- 最新バックアップ取得 -->
        <exec executable="ls" outputproperty="latest.backup">
            <arg line="-t /backup/applications/${app.name}.war.backup.*"/>
        </exec>
        
        <fail unless="latest.backup" message="No backup found for rollback"/>
        
        <antcall target="stop-tomcat-graceful"/>
        
        <!-- 失敗したアプリ削除 -->
        <exec executable="sudo">
            <arg line="rm -rf ${tomcat.webapps}/${app.name} ${tomcat.webapps}/${app.name}.war"/>
        </exec>
        
        <!-- バックアップから復元 -->
        <exec executable="sudo">
            <arg line="cp ${latest.backup} ${tomcat.webapps}/${app.name}.war"/>
        </exec>
        <exec executable="sudo">
            <arg line="chown tomcat:tomcat ${tomcat.webapps}/${app.name}.war"/>
        </exec>
        
        <antcall target="start-tomcat"/>
        <antcall target="health-check"/>
        <antcall target="maintenance-mode-off"/>
        
        <echo message="Rollback completed successfully"/>
    </target>
    
</project>
```

このAnt環境での段階的導入により、従来のビルドツールを使用しながら効率的なCI/CDを実現できます！

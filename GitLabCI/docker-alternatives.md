# GitLab CI/CDでのDocker利用 - 必須か？代替手段は？

## 結論：Dockerは必須ではありません

GitLab CI/CDでは**Dockerは必須ではありません**が、**デフォルトの実行環境**として広く使用されています。Dockerを使わない代替手段も存在します。

## GitLab Runner の実行環境（Executor）

GitLab CI/CDジョブを実行する「GitLab Runner」には複数の実行環境があります：

### 1. **Docker Executor**（最も一般的）
```yaml
# .gitlab-ci.yml
build:
  image: node:18    # Dockerイメージを指定
  script:
    - npm install
    - npm run build
```

**メリット**:
- 環境の一貫性が保証される
- 豊富な既製イメージが利用可能
- 環境の分離とクリーンアップが自動

**デメリット**:
- Dockerの知識が必要
- イメージのダウンロード時間
- リソース使用量が多い

### 2. **Shell Executor**（Dockerなし）
```yaml
# .gitlab-ci.yml
build:
  # imageを指定しない
  script:
    - node --version
    - npm install
    - npm run build
  tags:
    - shell-runner    # Shell Runnerを指定
```

**メリット**:
- Dockerが不要
- 高速（イメージダウンロード不要）
- ホストの全リソースを利用可能

**デメリット**:
- 環境の管理が複雑
- ジョブ間での環境汚染の可能性
- セキュリティリスク

### 3. **SSH Executor**
```yaml
# .gitlab-ci.yml
deploy:
  script:
    - ./deploy.sh
  tags:
    - ssh-runner
```

リモートサーバーにSSHで接続してジョブを実行

### 4. **その他のExecutor**
- **Kubernetes Executor**: Kubernetesクラスター上で実行
- **VirtualBox Executor**: VirtualBoxでVM作成
- **Parallels Executor**: Parallels Desktop使用

## Docker不要でGitLab CI/CDを使う方法

### Shell Executorの設定例

#### 1. GitLab Runnerの登録（Shell）

```bash
# Shell ExecutorでGitLab Runnerを登録
sudo gitlab-runner register \
  --non-interactive \
  --url "https://gitlab.example.com/" \
  --registration-token "YOUR_REGISTRATION_TOKEN" \
  --executor "shell" \
  --description "Shell Runner" \
  --tag-list "shell,linux" \
  --run-untagged="true"
```

#### 2. 前提条件の準備

```bash
# サーバーに必要なツールを直接インストール
# Java環境の場合
sudo yum install -y java-17-openjdk maven git

# Node.js環境の場合
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs

# Python環境の場合  
sudo yum install -y python3 python3-pip
```

#### 3. Shell用 .gitlab-ci.yml

```yaml
# Docker不要の.gitlab-ci.yml例

stages:
  - build
  - test
  - deploy

variables:
  # パス設定
  JAVA_HOME: "/usr/lib/jvm/java-17-openjdk"
  MAVEN_HOME: "/usr/share/maven"
  PATH: "$MAVEN_HOME/bin:$JAVA_HOME/bin:$PATH"

# Javaプロジェクトのビルド
build_java:
  stage: build
  script:
    - java -version
    - mvn --version
    - mvn clean compile
    - mvn package
  artifacts:
    paths:
      - target/*.jar
  tags:
    - shell    # Shell Runnerで実行

# Node.jsプロジェクトのビルド
build_nodejs:
  stage: build
  script:
    - node --version
    - npm --version
    - npm install
    - npm run build
  artifacts:
    paths:
      - dist/
  tags:
    - shell

# テスト実行
test:
  stage: test
  script:
    - mvn test
  dependencies:
    - build_java
  tags:
    - shell

# ローカルデプロイ
deploy_local:
  stage: deploy
  script:
    # Tomcatに直接デプロイ
    - sudo systemctl stop tomcat
    - sudo cp target/*.war /opt/tomcat/webapps/
    - sudo systemctl start tomcat
  dependencies:
    - build_java
  tags:
    - shell
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
      when: manual
```

### 複数環境の混在使用

```yaml
# 用途に応じてExecutorを使い分け

stages:
  - build
  - test
  - deploy

# Dockerを使用するジョブ
build_with_docker:
  stage: build
  image: maven:3.9.4-eclipse-temurin-17
  script:
    - mvn clean package
  artifacts:
    paths:
      - target/*.jar
  tags:
    - docker    # Docker Runnerで実行

# Shellを使用するジョブ
test_on_host:
  stage: test
  script:
    - /opt/custom-tools/security-scan.sh
  dependencies:
    - build_with_docker
  tags:
    - shell    # Shell Runnerで実行

# SSH経由でのリモートデプロイ
deploy_remote:
  stage: deploy
  script:
    - scp target/*.jar user@remote-server:/opt/app/
    - ssh user@remote-server "sudo systemctl restart myapp"
  dependencies:
    - build_with_docker
  tags:
    - ssh      # SSH Runnerで実行
```

## 各方式の比較

| 項目 | Docker Executor | Shell Executor | SSH Executor |
|------|-----------------|----------------|--------------|
| **環境の一貫性** | ◎ 完全に分離 | △ ホスト依存 | △ リモートホスト依存 |
| **セットアップ** | ○ 簡単 | △ 事前準備必要 | △ SSH設定必要 |
| **実行速度** | △ イメージDL時間 | ◎ 高速 | ○ ネットワーク次第 |
| **セキュリティ** | ◎ 分離された環境 | ⚠️ ホスト直接実行 | ⚠️ SSH経由 |
| **リソース使用** | △ 多い | ◎ 効率的 | ○ リモート使用 |
| **保守性** | ◎ 設定が簡単 | △ 環境管理複雑 | △ 複数サーバー管理 |

## Docker不要でCI/CDを実現する実践例

### 従来型Java Web環境での完全設定

```yaml
# Docker不要の完全なJava Web CI/CD

stages:
  - validate
  - build  
  - test
  - security
  - deploy

variables:
  # 環境設定
  JAVA_HOME: "/usr/lib/jvm/java-17-openjdk"
  MAVEN_HOME: "/usr/share/maven"
  TOMCAT_HOME: "/opt/tomcat"
  APP_NAME: "myapp"

# 環境検証
validate_environment:
  stage: validate
  script:
    - echo "=== Environment Validation ==="
    - java -version
    - mvn --version
    - echo "JAVA_HOME: $JAVA_HOME"
    - echo "MAVEN_HOME: $MAVEN_HOME"
    - df -h
    - free -m
  tags:
    - shell

# ソースコード検証
validate_source:
  stage: validate
  script:
    - echo "=== Source Code Validation ==="
    - find . -name "*.java" | wc -l
    - find . -name "pom.xml" | head -5
    - xmllint --noout pom.xml  # XML構文チェック
  tags:
    - shell

# Maven ビルド
build:
  stage: build
  before_script:
    # Mavenローカルリポジトリのキャッシュ
    - mkdir -p ~/.m2/repository
  script:
    - echo "=== Maven Build ==="
    - mvn clean compile
    - mvn package -DskipTests
    - ls -la target/
  after_script:
    # ビルド成果物の情報出力
    - |
      if [ -f target/*.war ]; then
        echo "WAR file created successfully:"
        ls -la target/*.war
      fi
  artifacts:
    paths:
      - target/*.war
      - target/classes/
    expire_in: 2 hours
  cache:
    key: maven-$CI_COMMIT_REF_SLUG
    paths:
      - ~/.m2/repository/
  tags:
    - shell

# ユニットテスト
test_unit:
  stage: test
  dependencies:
    - build
  script:
    - echo "=== Unit Tests ==="
    - mvn test
    - mvn surefire-report:report
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
    paths:
      - target/surefire-reports/
  tags:
    - shell

# 静的解析
security_scan:
  stage: security
  dependencies:
    - build
  script:
    - echo "=== Security Scan ==="
    # SpotBugs
    - mvn com.github.spotbugs:spotbugs-maven-plugin:check || true
    # Dependency Check
    - mvn org.owasp:dependency-check-maven:check || true
  artifacts:
    paths:
      - target/spotbugsXml.xml
      - target/dependency-check-report.html
  allow_failure: true
  tags:
    - shell

# ステージングデプロイ
deploy_staging:
  stage: deploy
  dependencies:
    - build
  before_script:
    - echo "=== Pre-deployment Checks ==="
    - sudo systemctl status tomcat
    - df -h $TOMCAT_HOME
  script:
    - echo "=== Staging Deployment ==="
    - WAR_FILE=$(ls target/*.war | head -n 1)
    - echo "Deploying: $WAR_FILE"
    
    # バックアップ作成
    - |
      if [ -f "$TOMCAT_HOME/webapps/$APP_NAME.war" ]; then
        sudo cp "$TOMCAT_HOME/webapps/$APP_NAME.war" \
               "$TOMCAT_HOME/webapps/$APP_NAME.war.backup.$(date +%Y%m%d_%H%M%S)"
      fi
    
    # Tomcat停止
    - sudo systemctl stop tomcat
    - sleep 10
    
    # 古いアプリ削除
    - sudo rm -rf "$TOMCAT_HOME/webapps/$APP_NAME"*
    
    # 新しいアプリデプロイ
    - sudo cp "$WAR_FILE" "$TOMCAT_HOME/webapps/$APP_NAME.war"
    - sudo chown tomcat:tomcat "$TOMCAT_HOME/webapps/$APP_NAME.war"
    
    # Tomcat起動
    - sudo systemctl start tomcat
    - sleep 30
    
    # ヘルスチェック
    - curl -f "http://localhost:8080/$APP_NAME/health" || curl -f "http://localhost:8080/$APP_NAME/"
  after_script:
    # デプロイ後の状態確認
    - sudo systemctl status tomcat
    - sudo tail -20 $TOMCAT_HOME/logs/catalina.out
  environment:
    name: staging
    url: http://staging-server:8080/$APP_NAME
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"
  tags:
    - shell

# 本番デプロイ
deploy_production:
  stage: deploy
  dependencies:
    - build
  script:
    - echo "=== Production Deployment ==="
    - WAR_FILE=$(ls target/*.war | head -n 1)
    
    # より慎重なデプロイ手順
    - ./scripts/production-deploy.sh "$WAR_FILE"
  environment:
    name: production
    url: https://production.example.com/$APP_NAME
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
      when: manual  # 手動承認必須
  tags:
    - shell
```

### 本番デプロイスクリプト（scripts/production-deploy.sh）

```bash
#!/bin/bash

# Shell Executor用の本番デプロイスクリプト

set -e

WAR_FILE="$1"
APP_NAME="myapp"
TOMCAT_HOME="/opt/tomcat"
BACKUP_DIR="/backup/applications"

if [ -z "$WAR_FILE" ]; then
    echo "Usage: $0 <war-file>"
    exit 1
fi

echo "=== Production Deployment Started ==="
echo "WAR File: $WAR_FILE"
echo "Timestamp: $(date)"

# 事前チェック
echo "--- Pre-deployment Checks ---"
sudo systemctl status tomcat
df -h $TOMCAT_HOME
free -m

# データベースバックアップ
echo "--- Database Backup ---"
mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > "$BACKUP_DIR/db_backup_$(date +%Y%m%d_%H%M%S).sql"

# アプリケーションバックアップ
echo "--- Application Backup ---"
if [ -f "$TOMCAT_HOME/webapps/$APP_NAME.war" ]; then
    sudo cp "$TOMCAT_HOME/webapps/$APP_NAME.war" \
           "$BACKUP_DIR/$APP_NAME.war.backup.$(date +%Y%m%d_%H%M%S)"
fi

# メンテナンスモード
echo "--- Maintenance Mode ON ---"
sudo cp /var/www/maintenance.html /var/www/html/index.html || true

# デプロイ実行
echo "--- Application Deployment ---"
sudo systemctl stop tomcat
sleep 15

sudo rm -rf "$TOMCAT_HOME/webapps/$APP_NAME"*
sudo cp "$WAR_FILE" "$TOMCAT_HOME/webapps/$APP_NAME.war"
sudo chown tomcat:tomcat "$TOMCAT_HOME/webapps/$APP_NAME.war"

sudo systemctl start tomcat

# ヘルスチェック
echo "--- Health Check ---"
for i in {1..30}; do
    if curl -f "http://localhost:8080/$APP_NAME/health" 2>/dev/null; then
        echo "✓ Application is healthy"
        break
    fi
    echo "Waiting for application... ($i/30)"
    sleep 10
done

# メンテナンスモード解除
echo "--- Maintenance Mode OFF ---"
sudo rm -f /var/www/html/index.html || true

echo "=== Production Deployment Completed ==="
```

## Docker vs Shell の選択指針

### **Dockerを選ぶべき場合**
- チーム開発での環境統一が重要
- 複数の言語・ツールを使用
- クラウド環境での実行
- CI/CD初心者

### **Shell Executorを選ぶべき場合**
- 既存サーバー環境が安定している
- パフォーマンスを最優先
- 特殊なツール・ライブラリが必要
- Docker導入が困難

### **混在使用のケース**
- ビルド：Docker（環境統一）
- テスト：Shell（性能テスト）  
- デプロイ：Shell（既存環境）

**結論**: GitLab CI/CDではDockerは必須ではありません。あなたの環境や要件に最適な実行方式を選択できます！

# Tomcat + EC2 環境での GitLab CI/CD 導入ガイド

## 概要
コンテナを使用せず、EC2インスタンス上のTomcatサーバーにWARファイルを直接デプロイするCI/CDパイプラインの構築方法を説明します。

## 全体のアーキテクチャ

```
GitLab → CI/CD Pipeline → WAR Build → EC2(Tomcat) → Application
```

### 従来の手動デプロイからCI/CDへの移行

**Before（手動）:**
1. 開発者がローカルでビルド
2. SCPでWARファイルをサーバーに転送
3. サーバーにSSHでログイン
4. Tomcatを停止
5. WARファイルを配置
6. Tomcatを再起動

**After（自動）:**
1. コードをGitLabにプッシュ
2. パイプラインが自動実行
3. 自動でビルド・テスト・デプロイ

## JavaアプリケーションのCI/CD設定

### .gitlab-ci.yml（Tomcat向け）

```yaml
stages:
  - build
  - test
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  MAVEN_CLI_OPTS: "--batch-mode --errors --fail-at-end --show-version"
  
# Maven依存関係キャッシュ
.maven_cache: &maven_cache
  cache:
    key: 
      files:
        - pom.xml
    paths:
      - .m2/repository/
    policy: pull-push

# Javaアプリケーションビルド
build_war:
  image: maven:3.9.4-eclipse-temurin-17
  <<: *maven_cache
  stage: build
  script:
    # プロファイル指定でビルド（本番用設定）
    - mvn $MAVEN_CLI_OPTS compile
    - mvn $MAVEN_CLI_OPTS package -P production
    # WARファイルの存在確認
    - ls -la target/*.war
    # バージョン情報をファイルに出力
    - mvn help:evaluate -Dexpression=project.version -q -DforceStdout > version.txt
  artifacts:
    paths:
      - target/*.war
      - version.txt
    expire_in: 1 hour
    reports:
      # Maven依存関係レポート
      dependencies:
        - target/dependency-check-report.xml
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# ユニットテスト
test_unit:
  image: maven:3.9.4-eclipse-temurin-17
  <<: *maven_cache
  stage: test
  dependencies:
    - build_war
  script:
    - mvn $MAVEN_CLI_OPTS test
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
    paths:
      - target/surefire-reports/
  coverage: '/Total.*?([0-9]{1,3})%/'
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# 結合テスト（オプション）
test_integration:
  image: maven:3.9.4-eclipse-temurin-17
  <<: *maven_cache
  stage: test
  services:
    - mysql:8.0
  variables:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: testdb
    MYSQL_USER: testuser
    MYSQL_PASSWORD: testpass
  dependencies:
    - build_war
  script:
    - mvn $MAVEN_CLI_OPTS verify -P integration-test
  artifacts:
    reports:
      junit:
        - target/failsafe-reports/TEST-*.xml
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH
  allow_failure: true

# ステージング環境デプロイ
deploy_staging:
  stage: deploy
  image: alpine:latest
  dependencies:
    - build_war
  before_script:
    - apk add --no-cache openssh-client rsync
    - eval $(ssh-agent -s)
    - echo "$STAGING_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$STAGING_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    # WARファイル名を取得
    - WAR_FILE=$(ls target/*.war | head -n 1)
    - APP_NAME=$(basename "$WAR_FILE" .war)
    - echo "Deploying $WAR_FILE to staging"
    
    # バックアップ作成
    - ssh $STAGING_USER@$STAGING_HOST "sudo cp $TOMCAT_WEBAPPS/$APP_NAME.war $TOMCAT_WEBAPPS/$APP_NAME.war.backup.$(date +%Y%m%d_%H%M%S) 2>/dev/null || true"
    
    # Tomcat停止
    - ssh $STAGING_USER@$STAGING_HOST "sudo systemctl stop tomcat"
    
    # 既存アプリケーション削除
    - ssh $STAGING_USER@$STAGING_HOST "sudo rm -rf $TOMCAT_WEBAPPS/$APP_NAME $TOMCAT_WEBAPPS/$APP_NAME.war"
    
    # 新しいWARファイルアップロード
    - scp "$WAR_FILE" $STAGING_USER@$STAGING_HOST:/tmp/
    - ssh $STAGING_USER@$STAGING_HOST "sudo mv /tmp/$(basename $WAR_FILE) $TOMCAT_WEBAPPS/"
    - ssh $STAGING_USER@$STAGING_HOST "sudo chown tomcat:tomcat $TOMCAT_WEBAPPS/$(basename $WAR_FILE)"
    
    # Tomcat起動
    - ssh $STAGING_USER@$STAGING_HOST "sudo systemctl start tomcat"
    
    # デプロイ確認（ヘルスチェック）
    - sleep 30
    - ssh $STAGING_USER@$STAGING_HOST "curl -f http://localhost:8080/$APP_NAME/health || curl -f http://localhost:8080/$APP_NAME/"
  environment:
    name: staging
    url: http://$STAGING_HOST:8080/$APP_NAME
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"
  retry:
    max: 2
    when: 
      - script_failure

# 本番環境デプロイ
deploy_production:
  stage: deploy
  image: alpine:latest
  dependencies:
    - build_war
  before_script:
    - apk add --no-cache openssh-client rsync
    - eval $(ssh-agent -s)
    - echo "$PRODUCTION_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$PRODUCTION_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    # WARファイル名を取得
    - WAR_FILE=$(ls target/*.war | head -n 1)
    - APP_NAME=$(basename "$WAR_FILE" .war)
    - VERSION=$(cat version.txt)
    - echo "Deploying $WAR_FILE (v$VERSION) to production"
    
    # メンテナンスページ表示（オプション）
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo cp /var/www/maintenance.html /var/www/html/index.html" || true
    
    # 現在のアプリケーションをバックアップ
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo cp $TOMCAT_WEBAPPS/$APP_NAME.war $TOMCAT_WEBAPPS/$APP_NAME.war.backup.$(date +%Y%m%d_%H%M%S) 2>/dev/null || true"
    
    # データベースバックアップ（必要に応じて）
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "mysqldump -u\$DB_USER -p\$DB_PASSWORD \$DB_NAME > /backup/db_backup_$(date +%Y%m%d_%H%M%S).sql" || true
    
    # Tomcat graceful shutdown
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl stop tomcat"
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sleep 10"
    
    # 既存アプリケーション削除
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo rm -rf $TOMCAT_WEBAPPS/$APP_NAME $TOMCAT_WEBAPPS/$APP_NAME.war"
    
    # 新しいWARファイルデプロイ
    - scp "$WAR_FILE" $PRODUCTION_USER@$PRODUCTION_HOST:/tmp/
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo mv /tmp/$(basename $WAR_FILE) $TOMCAT_WEBAPPS/"
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo chown tomcat:tomcat $TOMCAT_WEBAPPS/$(basename $WAR_FILE)"
    
    # Tomcat起動
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl start tomcat"
    
    # アプリケーション起動待機
    - echo "Waiting for application to start..."
    - sleep 60
    
    # ヘルスチェック
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "curl -f http://localhost:8080/$APP_NAME/health || curl -f http://localhost:8080/$APP_NAME/"
    
    # メンテナンスページ削除
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo rm /var/www/html/index.html" || true
    
    # ログ確認
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo tail -n 50 /opt/tomcat/logs/catalina.out"
    
  after_script:
    # デプロイ失敗時の自動ロールバック
    - |
      if [ $CI_JOB_STATUS == 'failed' ]; then
        echo "Deployment failed, initiating rollback..."
        WAR_FILE=$(ls target/*.war | head -n 1)
        APP_NAME=$(basename "$WAR_FILE" .war)
        
        # Tomcat停止
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl stop tomcat"
        
        # 失敗したWARファイル削除
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo rm -rf $TOMCAT_WEBAPPS/$APP_NAME $TOMCAT_WEBAPPS/$APP_NAME.war"
        
        # 最新のバックアップを復元
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "LATEST_BACKUP=\$(ls -t $TOMCAT_WEBAPPS/$APP_NAME.war.backup.* 2>/dev/null | head -n1); if [ -n \"\$LATEST_BACKUP\" ]; then sudo cp \"\$LATEST_BACKUP\" $TOMCAT_WEBAPPS/$APP_NAME.war; fi"
        
        # Tomcat再起動
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl start tomcat"
        
        echo "Rollback completed"
      fi
  environment:
    name: production
    url: https://$PRODUCTION_DOMAIN/$APP_NAME
  when: manual
  rules:
    - if: $CI_COMMIT_BRANCH == "main"

# デプロイ後のスモークテスト
smoke_test:
  stage: deploy
  image: alpine:latest
  dependencies: []
  before_script:
    - apk add --no-cache curl
  script:
    - WAR_FILE=$(ls target/*.war | head -n 1) || WAR_FILE="myapp.war"
    - APP_NAME=$(basename "$WAR_FILE" .war)
    # 基本的なエンドポイントテスト
    - curl -f https://$PRODUCTION_DOMAIN/$APP_NAME/health
    - curl -f https://$PRODUCTION_DOMAIN/$APP_NAME/api/status
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
  needs: ["deploy_production"]
```

## 必要な環境変数設定

GitLabプロジェクトのSettings → CI/CD → Variablesで以下を設定：

### SSH関連
| 変数名                       | 説明                        | 保護 | マスク |
| ---------------------------- | --------------------------- | ---- | ------ |
| `STAGING_SSH_PRIVATE_KEY`    | ステージング環境SSH秘密鍵   | ✓    | ✓      |
| `STAGING_SSH_KNOWN_HOSTS`    | ステージング環境known_hosts | -    | -      |
| `PRODUCTION_SSH_PRIVATE_KEY` | 本番環境SSH秘密鍵           | ✓    | ✓      |
| `PRODUCTION_SSH_KNOWN_HOSTS` | 本番環境known_hosts         | ✓    | -      |

### サーバー情報
| 変数名              | 説明                         | 例                       |
| ------------------- | ---------------------------- | ------------------------ |
| `STAGING_HOST`      | ステージングサーバーホスト   | `staging.example.com`    |
| `STAGING_USER`      | ステージングサーバーユーザー | `deploy`                 |
| `PRODUCTION_HOST`   | 本番サーバーホスト           | `production.example.com` |
| `PRODUCTION_USER`   | 本番サーバーユーザー         | `deploy`                 |
| `PRODUCTION_DOMAIN` | 本番ドメイン                 | `example.com`            |
| `TOMCAT_WEBAPPS`    | Tomcat webappsディレクトリ   | `/opt/tomcat/webapps`    |

## サーバー側の準備

### 1. デプロイ用ユーザー作成

```bash
# EC2サーバーで実行
sudo useradd -m -s /bin/bash deploy
sudo usermod -aG sudo deploy

# Tomcatグループに追加（必要に応じて）
sudo usermod -aG tomcat deploy

# SSH鍵ディレクトリ作成
sudo -u deploy mkdir -p /home/deploy/.ssh
sudo -u deploy chmod 700 /home/deploy/.ssh
```

### 2. SSH鍵ペア生成と設定

```bash
# ローカルマシンで実行
ssh-keygen -t ed25519 -C "gitlab-ci-deploy-$(date +%Y%m%d)" -f gitlab-ci-key

# 公開鍵をサーバーに追加
cat gitlab-ci-key.pub | ssh ec2-user@your-server "sudo -u deploy tee -a /home/deploy/.ssh/authorized_keys"

# 権限設定
ssh ec2-user@your-server "sudo -u deploy chmod 600 /home/deploy/.ssh/authorized_keys"

# 秘密鍵をGitLabの環境変数に設定（内容をコピー）
cat gitlab-ci-key
```

### 3. sudoers設定（パスワードなしでTomcat操作）

```bash
# /etc/sudoers.d/deploy ファイルを作成
sudo visudo /etc/sudoers.d/deploy

# 以下の内容を追加
deploy ALL=(ALL) NOPASSWD: /bin/systemctl start tomcat, /bin/systemctl stop tomcat, /bin/systemctl restart tomcat, /bin/systemctl status tomcat, /bin/cp, /bin/mv, /bin/rm, /bin/chown, /bin/chmod
```

### 4. Tomcatの自動デプロイ機能無効化

```xml
<!-- /opt/tomcat/conf/server.xml -->
<Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="false" deployOnStartup="true">
</Host>
```

### 5. ヘルスチェックエンドポイント作成

Javaアプリケーションにヘルスチェック用のエンドポイントを追加：

```java
@RestController
@RequestMapping("/health")
public class HealthController {
    
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", Instant.now().toString());
        status.put("version", getClass().getPackage().getImplementationVersion());
        return ResponseEntity.ok(status);
    }
}
```

## pom.xml設定例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>my-webapp</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>war</packaging>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring.version>5.3.21</spring.version>
    </properties>

    <profiles>
        <!-- 本番環境用プロファイル -->
        <profile>
            <id>production</id>
            <properties>
                <env>production</env>
                <db.url>jdbc:mysql://prod-db:3306/myapp</db.url>
            </properties>
            <build>
                <resources>
                    <resource>
                        <directory>src/main/resources</directory>
                        <filtering>true</filtering>
                    </resource>
                </resources>
            </build>
        </profile>
        
        <!-- 統合テスト用プロファイル -->
        <profile>
            <id>integration-test</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-failsafe-plugin</artifactId>
                        <version>3.1.2</version>
                        <executions>
                            <execution>
                                <goals>
                                    <goal>integration-test</goal>
                                    <goal>verify</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

    <build>
        <finalName>${project.artifactId}-${project.version}</finalName>
        <plugins>
            <!-- Mavenコンパイラプラグイン -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            
            <!-- WARプラグイン -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.1</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
            
            <!-- Surefireプラグイン（ユニットテスト） -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/*Tests.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!-- Spring Framework -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>${spring.version}</version>
        </dependency>
        
        <!-- テスト依存関係 -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

## セキュリティ強化

### 1. ファイアウォール設定

```bash
# EC2セキュリティグループまたはiptablesで設定
# SSH: 22番ポート（CI/CDからのみ）
# HTTP: 80番ポート
# HTTPS: 443番ポート
# Tomcat管理用: 8080番ポート（内部のみ）
```

### 2. Tomcat設定の強化

```xml
<!-- server.xml - 本番環境向け設定 -->
<Server port="8005" shutdown="SHUTDOWN">
    <!-- セキュリティ関連設定 -->
    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443"
               server=" "
               maxThreads="200" />
</Server>
```

### 3. アプリケーションログ監視

```bash
# ログローテーション設定 /etc/logrotate.d/tomcat
/opt/tomcat/logs/*.log {
    daily
    missingok
    rotate 52
    compress
    notifempty
    create 0644 tomcat tomcat
}
```

## トラブルシューティング

### よくある問題と解決法

1. **Tomcat起動失敗**
   ```bash
   # ログ確認
   sudo tail -f /opt/tomcat/logs/catalina.out
   
   # プロセス確認
   sudo systemctl status tomcat
   
   # ポート使用状況確認
   sudo netstat -tlnp | grep 8080
   ```

2. **メモリ不足**
   ```bash
   # /opt/tomcat/bin/setenv.sh
   export CATALINA_OPTS="-Xms512m -Xmx2048m -XX:MetaspaceSize=256m"
   ```

3. **デプロイタイムアウト**
   - WARファイルサイズの確認
   - ネットワーク帯域の確認
   - サーバーリソース（CPU/メモリ）の確認

このガイドを参考に、段階的にCI/CDを導入してください。まずはステージング環境で十分にテストしてから本番適用することをお勧めします！

## AntビルドアプリケーションのCI/CD設定

### .gitlab-ci.yml（Ant + Tomcat向け）

```yaml
stages:
  - build
  - test
  - deploy

variables:
  ANT_OPTS: "-Xmx1024m"
  JAVA_HOME: "/usr/lib/jvm/java-17-openjdk"
  
# Ant依存関係キャッシュ（libディレクトリ）
.ant_cache: &ant_cache
  cache:
    key: 
      files:
        - build.xml
        - ivy.xml  # Ivyを使用している場合
    paths:
      - lib/
      - build/classes/
    policy: pull-push

# Antアプリケーションビルド
build_war_ant:
  image: openjdk:17-jdk-slim
  <<: *ant_cache
  stage: build
  before_script:
    # Antのインストール
    - apt-get update && apt-get install -y ant
    # Ivyのセットアップ（依存関係管理を使用している場合）
    - if [ -f "ivy.xml" ]; then ant resolve; fi
  script:
    # 依存関係の解決とコンパイル
    - ant clean compile
    # WARファイルの作成（本番用プロパティファイル使用）
    - ant -Denv=production war
    # WARファイルの存在確認
    - ls -la dist/*.war
    # バージョン情報をファイルに出力（build.propertiesから取得）
    - ant version > version.txt || echo "1.0.0" > version.txt
  artifacts:
    paths:
      - dist/*.war
      - version.txt
    expire_in: 1 hour
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# Antユニットテスト
test_unit_ant:
  image: openjdk:17-jdk-slim
  <<: *ant_cache
  stage: test
  dependencies:
    - build_war_ant
  before_script:
    - apt-get update && apt-get install -y ant
  script:
    # JUnitテスト実行
    - ant test
    # テストレポートの生成
    - ant test-report
  artifacts:
    reports:
      junit:
        - build/test-results/TEST-*.xml
    paths:
      - build/test-results/
      - build/test-reports/
  coverage: '/Total.*?([0-9]{1,3})%/'
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH

# Ant結合テスト（オプション）
test_integration_ant:
  image: openjdk:17-jdk-slim
  <<: *ant_cache
  stage: test
  services:
    - mysql:8.0
  variables:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: testdb
    MYSQL_USER: testuser
    MYSQL_PASSWORD: testpass
  dependencies:
    - build_war_ant
  before_script:
    - apt-get update && apt-get install -y ant mysql-client
  script:
    # データベースセットアップ
    - mysql -h mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS testdb;"
    # 結合テスト実行
    - ant -Ddb.url=jdbc:mysql://mysql:3306/testdb -Ddb.user=testuser -Ddb.password=testpass integration-test
  artifacts:
    reports:
      junit:
        - build/integration-test-results/TEST-*.xml
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH
  allow_failure: true

# ステージング環境デプロイ（Ant版）
deploy_staging_ant:
  stage: deploy
  image: alpine:latest
  dependencies:
    - build_war_ant
  before_script:
    - apk add --no-cache openssh-client rsync
    - eval $(ssh-agent -s)
    - echo "$STAGING_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$STAGING_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    # WARファイル名を取得
    - WAR_FILE=$(ls dist/*.war | head -n 1)
    - APP_NAME=$(basename "$WAR_FILE" .war)
    - echo "Deploying $WAR_FILE to staging"
    
    # バックアップ作成
    - ssh $STAGING_USER@$STAGING_HOST "sudo cp $TOMCAT_WEBAPPS/$APP_NAME.war $TOMCAT_WEBAPPS/$APP_NAME.war.backup.$(date +%Y%m%d_%H%M%S) 2>/dev/null || true"
    
    # Tomcat停止
    - ssh $STAGING_USER@$STAGING_HOST "sudo systemctl stop tomcat"
    
    # 既存アプリケーション削除
    - ssh $STAGING_USER@$STAGING_HOST "sudo rm -rf $TOMCAT_WEBAPPS/$APP_NAME $TOMCAT_WEBAPPS/$APP_NAME.war"
    
    # 新しいWARファイルアップロード
    - scp "$WAR_FILE" $STAGING_USER@$STAGING_HOST:/tmp/
    - ssh $STAGING_USER@$STAGING_HOST "sudo mv /tmp/$(basename $WAR_FILE) $TOMCAT_WEBAPPS/"
    - ssh $STAGING_USER@$STAGING_HOST "sudo chown tomcat:tomcat $TOMCAT_WEBAPPS/$(basename $WAR_FILE)"
    
    # Tomcat起動
    - ssh $STAGING_USER@$STAGING_HOST "sudo systemctl start tomcat"
    
    # デプロイ確認（ヘルスチェック）
    - sleep 30
    - ssh $STAGING_USER@$STAGING_HOST "curl -f http://localhost:8080/$APP_NAME/health || curl -f http://localhost:8080/$APP_NAME/"
  environment:
    name: staging
    url: http://$STAGING_HOST:8080/$APP_NAME
  rules:
    - if: $CI_COMMIT_BRANCH == "develop"
  retry:
    max: 2
    when: 
      - script_failure

# 本番環境デプロイ（Ant版）
deploy_production_ant:
  stage: deploy
  image: alpine:latest
  dependencies:
    - build_war_ant
  before_script:
    - apk add --no-cache openssh-client rsync
    - eval $(ssh-agent -s)
    - echo "$PRODUCTION_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh && chmod 700 ~/.ssh
    - echo "$PRODUCTION_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    # WARファイル名を取得
    - WAR_FILE=$(ls dist/*.war | head -n 1)
    - APP_NAME=$(basename "$WAR_FILE" .war)
    - VERSION=$(cat version.txt)
    - echo "Deploying $WAR_FILE (v$VERSION) to production"
    
    # メンテナンスページ表示（オプション）
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo cp /var/www/maintenance.html /var/www/html/index.html" || true
    
    # 現在のアプリケーションをバックアップ
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo cp $TOMCAT_WEBAPPS/$APP_NAME.war $TOMCAT_WEBAPPS/$APP_NAME.war.backup.$(date +%Y%m%d_%H%M%S) 2>/dev/null || true"
    
    # データベースバックアップ（必要に応じて）
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "mysqldump -u\$DB_USER -p\$DB_PASSWORD \$DB_NAME > /backup/db_backup_$(date +%Y%m%d_%H%M%S).sql" || true
    
    # Tomcat graceful shutdown
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl stop tomcat"
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sleep 10"
    
    # 既存アプリケーション削除
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo rm -rf $TOMCAT_WEBAPPS/$APP_NAME $TOMCAT_WEBAPPS/$APP_NAME.war"
    
    # 新しいWARファイルデプロイ
    - scp "$WAR_FILE" $PRODUCTION_USER@$PRODUCTION_HOST:/tmp/
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo mv /tmp/$(basename $WAR_FILE) $TOMCAT_WEBAPPS/"
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo chown tomcat:tomcat $TOMCAT_WEBAPPS/$(basename $WAR_FILE)"
    
    # Tomcat起動
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl start tomcat"
    
    # アプリケーション起動待機
    - echo "Waiting for application to start..."
    - sleep 60
    
    # ヘルスチェック
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "curl -f http://localhost:8080/$APP_NAME/health || curl -f http://localhost:8080/$APP_NAME/"
    
    # メンテナンスページ削除
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo rm /var/www/html/index.html" || true
    
    # ログ確認
    - ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo tail -n 50 /opt/tomcat/logs/catalina.out"
    
  after_script:
    # デプロイ失敗時の自動ロールバック
    - |
      if [ $CI_JOB_STATUS == 'failed' ]; then
        echo "Deployment failed, initiating rollback..."
        WAR_FILE=$(ls dist/*.war | head -n 1)
        APP_NAME=$(basename "$WAR_FILE" .war)
        
        # Tomcat停止
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl stop tomcat"
        
        # 失敗したWARファイル削除
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo rm -rf $TOMCAT_WEBAPPS/$APP_NAME $TOMCAT_WEBAPPS/$APP_NAME.war"
        
        # 最新のバックアップを復元
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "LATEST_BACKUP=\$(ls -t $TOMCAT_WEBAPPS/$APP_NAME.war.backup.* 2>/dev/null | head -n1); if [ -n \"\$LATEST_BACKUP\" ]; then sudo cp \"\$LATEST_BACKUP\" $TOMCAT_WEBAPPS/$APP_NAME.war; fi"
        
        # Tomcat再起動
        ssh $PRODUCTION_USER@$PRODUCTION_HOST "sudo systemctl start tomcat"
        
        echo "Rollback completed"
      fi
  environment:
    name: production
    url: https://$PRODUCTION_DOMAIN/$APP_NAME
  when: manual
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

## Ant環境でのサーバー側設定

### 1. Antランタイムの準備

```bash
# EC2サーバーでのAnt環境セットアップ
sudo yum update -y  # Amazon Linux 2の場合
sudo yum install -y java-17-openjdk java-17-openjdk-devel

# Antのインストール
cd /opt
sudo wget https://downloads.apache.org/ant/binaries/apache-ant-1.10.14-bin.tar.gz
sudo tar xzf apache-ant-1.10.14-bin.tar.gz
sudo mv apache-ant-1.10.14 ant
sudo chown -R root:root ant

# 環境変数設定
echo 'export ANT_HOME=/opt/ant' | sudo tee -a /etc/environment
echo 'export PATH=$PATH:$ANT_HOME/bin' | sudo tee -a /etc/environment
source /etc/environment

# Antバージョン確認
ant -version
```

### 2. プロジェクト固有の設定ファイル

#### build.local.properties（サーバー固有設定）

```properties
# サーバー固有設定
tomcat.home=/opt/tomcat
webapps.dir=${tomcat.home}/webapps
deploy.user=deploy

# データベース接続設定（本番環境）
db.url=jdbc:mysql://localhost:3306/production_db
db.user=app_user
db.password=secure_password

# ログ設定
log.dir=/var/log/myapp
log.level=INFO

# メール設定（エラー通知等）
mail.smtp.host=smtp.example.com
mail.from=noreply@example.com
```

### 3. デプロイ用Antスクリプト（deploy.xml）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="Deploy" default="deploy" basedir=".">
    
    <property file="deploy.properties"/>
    
    <!-- デプロイタスク -->
    <target name="deploy" description="Deploy WAR to Tomcat">
        <antcall target="backup-current"/>
        <antcall target="stop-tomcat"/>
        <antcall target="remove-old-app"/>
        <antcall target="install-new-app"/>
        <antcall target="start-tomcat"/>
        <antcall target="verify-deployment"/>
    </target>
    
    <!-- 現在のアプリケーションバックアップ -->
    <target name="backup-current">
        <tstamp>
            <format property="backup.timestamp" pattern="yyyyMMdd_HHmmss"/>
        </tstamp>
        
        <copy file="${tomcat.webapps}/${app.name}.war" 
              tofile="${backup.dir}/${app.name}.war.backup.${backup.timestamp}"
              failonerror="false"/>
    </target>
    
    <!-- Tomcat停止 -->
    <target name="stop-tomcat">
        <exec executable="sudo" failonerror="true">
            <arg line="systemctl stop tomcat"/>
        </exec>
        <sleep seconds="10"/>
    </target>
    
    <!-- 古いアプリケーション削除 -->
    <target name="remove-old-app">
        <delete dir="${tomcat.webapps}/${app.name}" failonerror="false"/>
        <delete file="${tomcat.webapps}/${app.name}.war" failonerror="false"/>
    </target>
    
    <!-- 新しいアプリケーション配置 -->
    <target name="install-new-app">
        <copy file="${war.file}" todir="${tomcat.webapps}"/>
        <chown file="${tomcat.webapps}/${app.name}.war" owner="tomcat" group="tomcat"/>
        <chmod file="${tomcat.webapps}/${app.name}.war" perm="644"/>
    </target>
    
    <!-- Tomcat開始 -->
    <target name="start-tomcat">
        <exec executable="sudo" failonerror="true">
            <arg line="systemctl start tomcat"/>
        </exec>
        <sleep seconds="30"/>
    </target>
    
    <!-- デプロイ確認 -->
    <target name="verify-deployment">
        <waitfor maxwait="5" maxwaitunit="minute" timeoutproperty="deploy.timeout">
            <http url="http://localhost:8080/${app.name}/health"/>
        </waitfor>
        
        <fail if="deploy.timeout" message="Deployment verification failed - application not responding"/>
        
        <echo message="Deployment successful - application is responding"/>
    </target>
    
    <!-- ロールバック -->
    <target name="rollback" description="Rollback to previous version">
        <input message="Are you sure you want to rollback? (y/n)" validargs="y,n" addproperty="rollback.confirm"/>
        
        <condition property="rollback.proceed">
            <equals arg1="${rollback.confirm}" arg2="y"/>
        </condition>
        
        <fail unless="rollback.proceed" message="Rollback cancelled by user"/>
        
        <!-- 最新のバックアップファイルを取得 -->
        <pathconvert property="latest.backup">
            <last>
                <sort>
                    <fileset dir="${backup.dir}" includes="${app.name}.war.backup.*"/>
                    <date/>
                </sort>
            </last>
        </pathconvert>
        
        <fail unless="latest.backup" message="No backup file found for rollback"/>
        
        <antcall target="stop-tomcat"/>
        <antcall target="remove-old-app"/>
        
        <copy file="${latest.backup}" tofile="${tomcat.webapps}/${app.name}.war"/>
        <chown file="${tomcat.webapps}/${app.name}.war" owner="tomcat" group="tomcat"/>
        
        <antcall target="start-tomcat"/>
        <antcall target="verify-deployment"/>
        
        <echo message="Rollback completed successfully"/>
    </target>
    
</project>
```

#### deploy.properties

```properties
# デプロイ設定
app.name=myapp
war.file=/tmp/myapp.war
tomcat.home=/opt/tomcat
tomcat.webapps=${tomcat.home}/webapps
backup.dir=/backup/applications

# タイムアウト設定
startup.timeout=300
health.check.retries=30
```

## Ant環境でのトラブルシューティング

### 1. よくある問題と解決法

#### ビルド関連
```bash
# Ant実行時のメモリ不足
export ANT_OPTS="-Xmx2048m -XX:MaxPermSize=512m"

# 日本語文字化け対策
export ANT_OPTS="$ANT_OPTS -Dfile.encoding=UTF-8"

# ビルドログの詳細出力
ant -verbose war
```

#### 依存関係の問題
```bash
# Ivyキャッシュのクリア
ant clean-cache  # ivy.xmlで定義が必要

# 手動でJARファイルをlibディレクトリに配置
mkdir -p lib
wget -O lib/mysql-connector-java.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
```

#### コンパイルエラー
```bash
# Javaバージョン確認
java -version
javac -version

# クラスパス確認
ant -diagnostics

# 詳細なコンパイルエラー表示
ant -Dbuild.compiler.pedantic=true compile
```

### 2. デバッグ用のAntターゲット

```xml
<!-- build.xmlに追加 -->

<!-- 環境情報表示 -->
<target name="debug-info" description="Display environment information">
    <echo message="Java Version: ${java.version}"/>
    <echo message="Java Home: ${java.home}"/>
    <echo message="Ant Version: ${ant.version}"/>
    <echo message="User Name: ${user.name}"/>
    <echo message="Operating System: ${os.name} ${os.version}"/>
    <echo message="Project Directory: ${basedir}"/>
    
    <path id="debug.classpath">
        <fileset dir="${lib.dir}" includes="*.jar"/>
    </path>
    <pathconvert pathsep="${line.separator}" property="classpath.display" refid="debug.classpath"/>
    <echo message="Classpath:${line.separator}${classpath.display}"/>
</target>

<!-- 依存関係確認 -->
<target name="check-dependencies" description="Check required dependencies">
    <condition property="mysql.jar.exists">
        <available file="${lib.dir}/mysql-connector-java.jar"/>
    </condition>
    <fail unless="mysql.jar.exists" message="MySQL connector JAR not found in ${lib.dir}"/>
    
    <condition property="servlet.jar.exists">
        <available file="${lib.dir}/javax.servlet-api.jar"/>
    </condition>
    <fail unless="servlet.jar.exists" message="Servlet API JAR not found in ${lib.dir}"/>
    
    <echo message="All required dependencies are available"/>
</target>

<!-- 設定値確認 -->
<target name="check-config" description="Validate configuration">
    <fail unless="version" message="Version property not set"/>
    <fail unless="env" message="Environment property not set"/>
    
    <echo message="Configuration validation passed"/>
    <echo message="Version: ${version}"/>
    <echo message="Environment: ${env}"/>
</target>
```

### 3. CI/CD パイプライン用デバッグ設定

```yaml
# .gitlab-ci.yml のデバッグ用設定を追加

# デバッグ情報収集
debug_build:
  image: openjdk:17-jdk-slim
  stage: build
  before_script:
    - apt-get update && apt-get install -y ant
  script:
    - echo "=== Environment Information ==="
    - ant debug-info
    - echo "=== Dependencies Check ==="
    - ant check-dependencies
    - echo "=== Configuration Check ==="
    - ant check-config
    - echo "=== Available Files ==="
    - find . -name "*.jar" -o -name "*.xml" -o -name "*.properties" | sort
  rules:
    - if: $CI_COMMIT_MESSAGE =~ /\[debug\]/
  when: manual
```

### 4. パフォーマンス最適化

```xml
<!-- build.xml のパフォーマンス改善設定 -->

<!-- 並列コンパイル -->
<target name="compile-parallel" depends="init">
    <javac srcdir="${src.dir}" 
           destdir="${classes.dir}"
           classpathref="classpath"
           includeantruntime="false"
           debug="true"
           encoding="UTF-8"
           fork="true"
           memoryMaximumSize="1024m">
        <compilerarg value="-J-Xmx1024m"/>
        <compilerarg value="-Xlint:unchecked"/>
    </javac>
</target>

<!-- 増分ビルド -->
<target name="compile-incremental" depends="init">
    <javac srcdir="${src.dir}" 
           destdir="${classes.dir}"
           classpathref="classpath"
           includeantruntime="false"
           debug="true"
           encoding="UTF-8">
        <!-- 変更されたファイルのみコンパイル -->
        <include name="**/*.java"/>
        <depend srcdir="${src.dir}" destdir="${classes.dir}"/>
    </javac>
</target>
```

これらの設定により、Ant環境でも効率的なCI/CDパイプラインを構築できます！

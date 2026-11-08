# AWS デプロイ例

## 概要
GitLab CI/CDを使用してAWSの各サービスにデプロイする方法を説明します。

## EC2インスタンスへのデプロイ

### 基本的なEC2デプロイ

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - deploy

variables:
  AWS_DEFAULT_REGION: ap-northeast-1

# ビルド
build:
  stage: build
  image: node:18
  script:
    - npm ci
    - npm run build
  artifacts:
    paths:
      - dist/
    expire_in: 1 hour

# EC2デプロイ
deploy_ec2:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client rsync
    - eval $(ssh-agent -s)
    - echo "$EC2_SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - echo "$EC2_SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
  script:
    # アプリケーションファイルをアップロード
    - rsync -avz --delete dist/ ec2-user@$EC2_HOST:/var/www/html/
    # サービス再起動
    - ssh ec2-user@$EC2_HOST "sudo systemctl reload nginx"
    - ssh ec2-user@$EC2_HOST "sudo systemctl restart myapp"
  environment:
    name: production
    url: https://example.com
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

## S3 + CloudFront（静的サイト）デプロイ

### S3静的ホスティング

```yaml
deploy_s3:
  stage: deploy
  image: amazon/aws-cli:latest
  before_script:
    - yum install -y nodejs npm
  script:
    - npm ci
    - npm run build
    # S3にアップロード
    - aws s3 sync dist/ s3://$S3_BUCKET_NAME --delete
    # CloudFrontキャッシュ無効化
    - aws cloudfront create-invalidation --distribution-id $CLOUDFRONT_DISTRIBUTION_ID --paths "/*"
  environment:
    name: production
    url: https://example.com
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

### より高度なS3デプロイ

```yaml
# キャッシュ制御付きS3デプロイ
deploy_s3_advanced:
  stage: deploy
  image: amazon/aws-cli:latest
  before_script:
    - yum install -y nodejs npm
    - npm ci
    - npm run build
  script:
    # HTMLファイル（キャッシュなし）
    - aws s3 sync dist/ s3://$S3_BUCKET_NAME --exclude="*.js" --exclude="*.css" --exclude="*.png" --exclude="*.jpg" --cache-control="no-cache" --delete
    # JS/CSSファイル（長期キャッシュ）
    - aws s3 sync dist/ s3://$S3_BUCKET_NAME --exclude="*.html" --cache-control="max-age=31536000" --delete
    # CloudFrontキャッシュ無効化（HTMLのみ）
    - aws cloudfront create-invalidation --distribution-id $CLOUDFRONT_DISTRIBUTION_ID --paths "/*.html" "/index.html"
  environment:
    name: production
```

## ECS（Elastic Container Service）デプロイ

### Fargateデプロイ

```yaml
stages:
  - build
  - push
  - deploy

variables:
  AWS_DEFAULT_REGION: ap-northeast-1
  ECR_REGISTRY: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
  ECR_REPOSITORY: myapp
  IMAGE_TAG: $CI_COMMIT_SHA

# ECRにDockerイメージプッシュ
push_to_ecr:
  stage: push
  image: docker:20.10.16
  services:
    - docker:20.10.16-dind
  before_script:
    - apk add --no-cache aws-cli
    - aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
  script:
    - docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
    - docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

# ECSサービス更新
deploy_ecs:
  stage: deploy
  image: amazon/aws-cli:latest
  script:
    # タスク定義を更新
    - |
      aws ecs describe-task-definition --task-definition $ECS_TASK_DEFINITION --query taskDefinition > task-def.json
      cat task-def.json | jq --arg IMAGE "$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" '.containerDefinitions[0].image = $IMAGE' | jq 'del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .placementConstraints, .compatibilities, .registeredAt, .registeredBy)' > new-task-def.json
    # 新しいタスク定義を登録
    - aws ecs register-task-definition --cli-input-json file://new-task-def.json
    # サービス更新
    - aws ecs update-service --cluster $ECS_CLUSTER_NAME --service $ECS_SERVICE_NAME --task-definition $ECS_TASK_DEFINITION
    # デプロイ完了まで待機
    - aws ecs wait services-stable --cluster $ECS_CLUSTER_NAME --services $ECS_SERVICE_NAME
  environment:
    name: production
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

## Lambda関数デプロイ

### Serverless Frameworkを使用

```yaml
deploy_lambda:
  stage: deploy
  image: node:18
  before_script:
    - npm install -g serverless
    - npm ci
  script:
    - serverless deploy --stage production
  environment:
    name: production
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

### AWS SAMを使用

```yaml
deploy_sam:
  stage: deploy
  image: amazon/aws-sam-cli-build-image-nodejs18.x
  script:
    - sam build
    - sam deploy --no-confirm-changeset --no-fail-on-empty-changeset --stack-name myapp-prod --s3-bucket $SAM_DEPLOYMENT_BUCKET --capabilities CAPABILITY_IAM --region $AWS_DEFAULT_REGION
  environment:
    name: production
```

## Elastic Beanstalkデプロイ

```yaml
deploy_beanstalk:
  stage: deploy
  image: python:3.9
  before_script:
    - pip install awsebcli
  script:
    - eb init -p "Node.js 18" myapp --region $AWS_DEFAULT_REGION
    - eb deploy production-env
  environment:
    name: production
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

## 必要なAWS権限設定

### IAMユーザー作成とポリシー

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-bucket-name",
        "arn:aws:s3:::your-bucket-name/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "cloudfront:CreateInvalidation"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecs:DescribeTaskDefinition",
        "ecs:RegisterTaskDefinition",
        "ecs:UpdateService",
        "ecs:DescribeServices"
      ],
      "Resource": "*"
    }
  ]
}
```

## 環境変数設定

GitLabのSettings → CI/CD → Variablesで設定：

| 変数名                       | 説明                               | 保護 |
| ---------------------------- | ---------------------------------- | ---- |
| `AWS_ACCESS_KEY_ID`          | AWSアクセスキーID                  | ✓    |
| `AWS_SECRET_ACCESS_KEY`      | AWSシークレットキー                | ✓    |
| `AWS_ACCOUNT_ID`             | AWSアカウントID                    | -    |
| `S3_BUCKET_NAME`             | S3バケット名                       | -    |
| `CLOUDFRONT_DISTRIBUTION_ID` | CloudFrontディストリビューションID | -    |
| `ECS_CLUSTER_NAME`           | ECSクラスター名                    | -    |
| `ECS_SERVICE_NAME`           | ECSサービス名                      | -    |
| `ECS_TASK_DEFINITION`        | ECSタスク定義名                    | -    |

## ブルーグリーンデプロイ

### ECSでのブルーグリーンデプロイ

```yaml
deploy_blue_green:
  stage: deploy
  image: amazon/aws-cli:latest
  script:
    # 現在のタスク数を取得
    - DESIRED_COUNT=$(aws ecs describe-services --cluster $ECS_CLUSTER_NAME --services $ECS_SERVICE_NAME --query 'services[0].desiredCount')
    
    # 新しいタスク定義でサービス更新
    - aws ecs update-service --cluster $ECS_CLUSTER_NAME --service $ECS_SERVICE_NAME --task-definition $NEW_TASK_DEFINITION
    
    # ヘルスチェック
    - |
      for i in {1..30}; do
        HEALTH=$(aws elbv2 describe-target-health --target-group-arn $TARGET_GROUP_ARN --query 'TargetHealthDescriptions[0].TargetHealth.State' --output text)
        if [ "$HEALTH" = "healthy" ]; then
          echo "Health check passed"
          break
        fi
        echo "Waiting for health check... ($i/30)"
        sleep 30
      done
    
    # 古いタスクを停止
    - aws ecs update-service --cluster $ECS_CLUSTER_NAME --service $ECS_SERVICE_NAME --desired-count $DESIRED_COUNT
```

## セキュリティ強化

### AssumeRoleを使用したデプロイ

```yaml
.aws_assume_role: &aws_assume_role
  before_script:
    - apk add --no-cache aws-cli jq
    - |
      CREDENTIALS=$(aws sts assume-role --role-arn $AWS_ROLE_ARN --role-session-name gitlab-ci-$CI_JOB_ID)
      export AWS_ACCESS_KEY_ID=$(echo $CREDENTIALS | jq -r .Credentials.AccessKeyId)
      export AWS_SECRET_ACCESS_KEY=$(echo $CREDENTIALS | jq -r .Credentials.SecretAccessKey)
      export AWS_SESSION_TOKEN=$(echo $CREDENTIALS | jq -r .Credentials.SessionToken)

deploy_with_assume_role:
  stage: deploy
  image: amazon/aws-cli:latest
  <<: *aws_assume_role
  script:
    - aws s3 sync dist/ s3://$S3_BUCKET_NAME --delete
```

## モニタリングとアラート

### CloudWatchアラーム設定

```yaml
setup_monitoring:
  stage: deploy
  image: amazon/aws-cli:latest
  script:
    # アプリケーションエラー率アラーム
    - |
      aws cloudwatch put-metric-alarm \
        --alarm-name "myapp-error-rate" \
        --alarm-description "Application error rate too high" \
        --metric-name ErrorRate \
        --namespace AWS/ApplicationELB \
        --statistic Average \
        --period 300 \
        --threshold 5.0 \
        --comparison-operator GreaterThanThreshold \
        --evaluation-periods 2
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

これらの例を参考に、あなたのプロジェクトとAWSの要件に合わせてカスタマイズしてください。

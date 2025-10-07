# GitLab Runnerのインストール
```sh
export GITLAB_RUNNER_REPO="https://packages.gitlab.com/install/repositories/runner/gitlab_runner/script.rpm.sh"
curl -L $GITLAB_RUNNER_REPO | sudo bash
# インストールしたいGitLabRunnerのバージョンを確認する
sudo dnf list gitlab-runner  --showduplicates | sort -r
sudo dnf -y install <インストールしたいGitlabRunnerのバージョン>
```

# Gitlabの画面から認証トークンを発行する
例:
`XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

# 発行した認証トークンをGitLabRunnerに登録する
```sh:
export RUNNER_AUTH_TOKEN="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
export GITLAB_SERVER_URL="https://gitlab.com"
sudo gitlab-runner register \
  --non-interactive \
  --url $GITLAB_SERVER_URL \
  --token "$RUNNER_AUTH_TOKEN" \
  --executor "shell" \
  --description "shell-executor-rhel"
```

Gitlabの画面からGitlabRunnerが認識されていれば成功

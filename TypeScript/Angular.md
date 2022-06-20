## Angularのインストール
```
yarn global add @angular/cli
```

### パッケージマネージャをYarnに設定する
```
ng config -g cli.packageManager yarn
```

### プロジェクト作成コマンド
```
ng new new-app-name
```

`new-app-name`は任意の名前

### Dockerコンテナ内で起動したAngularアプリを閲覧するために
`new-app-name/package.json`の`ng start`の設定を次のように書き換える。
```
"start": "ng serve --host 0.0.0.0 --port 4200",
```

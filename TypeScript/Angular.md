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

#### (補足)Angular quickstart
```
https://github.com/angular/quickstart
```

#### (補足)npmについて
`npm`はNode Package Managerを利用するためのコマンドでRubyにおける`gem`、PHPにおける`composer`、.NET Frameworkの`nuget`に相当する。

#### AngularでのHTTPサーバー
Angularでは`lite-server`というHTTPサーバーが標準で提供されており、自前でサーバーを構築する必要はない。

## コンポーネントの定義
コンポーネントの定義には`@angular/core`の`Component`をimportする必要がある。
```typescript:main.tsx
import { Component } from '@angular/core';

@Component({
    // ここにコンポーネントの内容を記述
})
```

### `main.ts`内の関数について
`platformBrowserDynamic()`と`bootstrapModule()`はそれぞれブラウザーでアプリを起動するためのAngular標準関数。
```typescript:main.ts
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule } from './app.module';

platformBrowserDynamic().bootstrapModule(AppModule);
```

## 演算子解説
### `?.`演算子
オブジェクトが空ではない場合だけプロパティにアクセスするように制御する演算子

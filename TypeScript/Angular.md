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

## ルーティング制御について

ルーターが基点とするパスで`<base>`要素で宣言する。
```html:route/src/index.html
<!DOCTYPE html>
<html>
<head>
<base href="/">
</head>
```

### ルートの定義
リクエストURLに応じて処理の受け渡し先となるコンポーネントを決定するように記述する。

app.routing.tsにコンポーネントのパスとルーティングテーブルを定義し、RouterModuleの`forRoot`メソッドに渡す。

```TypeScript:app.routing.ts
// コンポーネントのパスの追加
import { MainComponent } from './main.component';
import { ExampleComponent } from './example.component';
import { ErrorComponent } from './error.component';

// ルーティングの定義
const Route = [
    { path: 'exam', component: ExampleComponent },
    { path: '', component: MainComponent },
    { path: '**', component: ExampleComponent },
];

export const My_Routes: ModuleWithProviders = RouterModule.forRoot(Route);
```

## コンポーネントのライフサイクル
コンポーネントは最初に生成されると、プロパティや子コンポーネントの変化を受けて表示を変化させていき、非表示になるタイミングで破棄される。

`コンポーネント生成`->`コンストラクタ`->`ngOnChanges`->`ngOnInit`->`ngDoCheck`->`ngAfterContentInit`->`ngAfterContentChecked`->`ngAfterViewInit`->`ngAfterViewChecked`->`ngOnDestroyed`->`コンポーネント破棄`

# Angularによるフォーム開発
## 基本的なフォーム
具体的なサンプルでフォームの用法を大まかに俯瞰する。
```typescript:
// /src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
// フォーム機能のインポート
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

@NgModule({
    imports: [ BrowserModule, FormsModule ],  // FormModuleモジュールでフォーム機能を有効化する
    declarations: [ AppComponent ],
    bootstrap: [ AppComponent ]
})
export class AppModule {}
```

コンポーネントにhtml要素を記述していく
```typescript:/src/app/app.component.ts
// /src/app/app.component.ts
import { Component } from '@angular/core';

@Component({
    selector: "my-app",
    template: `
      <form #myForm="ngForm" (ngSubmit)="show()" novalidate>
        <div>
          <label for="mail">メールアドレス:</label><br />
        </div>
      </form>
    `
})
```

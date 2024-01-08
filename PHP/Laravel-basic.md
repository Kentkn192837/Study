- [Laravelの基本フォルダ- Laravelの基本フォルダ](#laravelの基本フォルダ--laravelの基本フォルダ)
  - [/config](#config)
  - [/app](#app)
  - [/public](#public)
  - [/resources](#resources)
  - [/routes](#routes)
  - [/database](#database)
- [Controllerの実装](#controllerの実装)
  - [作成したControllerに処理を記述する。](#作成したcontrollerに処理を記述する)
    - [web.phpが呼ばれる仕組み](#webphpが呼ばれる仕組み)
- [bladeを使って、テンプレートを作成する。](#bladeを使ってテンプレートを作成する)
  - [bladeテンプレートの命名規則](#bladeテンプレートの命名規則)
  - [テンプレートに値を渡す方法](#テンプレートに値を渡す方法)

# Laravelの基本フォルダ- [Laravelの基本フォルダ](#laravelの基本フォルダ)
## /config
アプリケーション全体の設定を記述するフォルダ。

## /app
プログラムを記述するフォルダ。`Console`, `Exception`, `Models`などといったフォルダが格納されており、用途に合わせて`.php`のプログラムを記述していく。

## /public
CSSやJavaScript、固定画像ファイルなどを配置するフォルダ。このフォルダが公開フォルダとなる。
基本的に大きな変更の無いファイルが中心となる。

## /resources
テンプレートとなるHTML部分を記述するファイルを格納するフォルダ。
外部から`/resources`内のリソースに直接アクセスすることはできない。
プログラムが読み込みに行くファイルを配置する。

## /routes
ルーティングの設定を記述するファイルを格納する。URLとControllerのマッピングを行う。

## /database
DB関連のプログラムを配置する。マイグレーションで利用するのがメイン。
`Models`ではページにアクセスされた時に、DBからデータを取得したり、DBのデータを更新する処理を行う。
`/database`フォルダに配置するプログラムの処理は、DBの初期テーブルや初期データを投入するなどの
開発レベルのデータベース操作のプログラムを配置することになる。


# Controllerの実装
Controllerを新規作成する場合は、`artisan`(アルチザン)コマンドを利用する。
```
php artisan make:controller 新規作成するコントローラ名
```

`IndexController`というコントローラを作成したい場合は、
```
php artisan make:controller IndexController
```

とする。
上記のコマンドを実行すると、`/app/Http/Controllers/IndexController.php`が作成される。

## 作成したControllerに処理を記述する。
`php artisan`で自動生成したコントローラには、まだ何も処理が記述されていない。

```php:IndexController.php
class IndexController extends Controller
{
    //
}
```

まだ何も処理が記述されていない`IndexController`クラスに処理を追記していく。
今回は、`IndexController`クラスの内部に`index`というメソッドを作成して、
このindexメソッド内に処理を記述する。

```php:IndexController.php
namespace App\Http\Controllers;
class IndexController extends Controller
{
    public function index() {
        print("<h1>Hello Laravel</h1>");
        return null;
    }
}
```

次に`/routes/web.php`を編集して、作成したコントローラのルーティングを設定する。
初期の`web.php`は、以下のような内容になっている（はず）。
```php:web.php
<?php
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});
```

`web.php`に`use`キーワードを使って、`IndexController`クラスが格納されている名前空間を呼び出す。
```php:web.php
<?php
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\IndexController;

Route::get('/', function () {
    return view('welcome');
});
```

既存の`Route::get('/', function (){...}`を消去して、以下のように
処理を追加する。
```php:web.php
<?php
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\IndexController;

Route::get('/', [IndexController::class, 'index']);
```

`get`で`/`にアクセスされたら、`IndexController`クラスの`index()`を実行しろ。という命令。

### web.phpが呼ばれる仕組み
`/app/Providers/RouteServiceProvider.php`の`boot()`にapiを返すのか、webを返すのかの設定が記述されている。
```php:RouteServiceProvider.php
// 一例
public function boot()
{
    $this->configureRateLimiting();

    $this->routes(function () {
        Route::middleware('api')
            ->prefix('api')
            ->group(base_path('routes/api.php'));

        Route::middleware('web')
            ->group(base_path('routes/web.php'));
    });
}
```

# bladeを使って、テンプレートを作成する。
コントローラクラスの内部に、HTMLを記述するという方法は分量が多く、
非現実的な方法である。そのため、Laravelで標準装備されているテンプレートエンジンのbladeを用いて、
処理結果として返却するHTMLをテンプレート化する。
テンプレート化したファイルは、`Controller`クラスで指定して、呼び出すことになる。
テンプレートは、`view()`を使うことで呼び出すことができる。

```php:IndexController.php
namespace App\Http\Controllers;
class IndexController extends Controller
{
    public function index() {
        return view('index');
    }
}
```

## bladeテンプレートの命名規則
bladeテンプレートを利用するファイルの命名規則は、`任意.blade.php`のように、
任意の部分以下は`blade.php`としなければならない。
`任意`の部分で命名した名前を`view()`に指定することで、指定したテンプレートを返却することができる。

## テンプレートに値を渡す方法
bladeで作成したテンプレートに値を渡す場合は、`view()`の第二引数に連想配列として渡す。
以下に例を示す。
```php:IndexController.php
public function index() {
    return view('index', ['message' => 'hello', 'name' => 'Taro']);
}
```

これを受け取る場合は、以下のように記述する。
```php:public/index.blade.php
<!-- 略 -->
<div id="mainContent">
  <p>{{$massage}}! {{$name}}さん</p>
</div>
<!-- 略 -->
```

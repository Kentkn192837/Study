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
  - [データにアクセス](#データにアクセス)
  - [DBの設定](#dbの設定)
  - [マイグレーションファイルの生成](#マイグレーションファイルの生成)
    - [空のマイグレーションファイルの作成](#空のマイグレーションファイルの作成)
  - [マイグレーションファイルからテーブルを作成](#マイグレーションファイルからテーブルを作成)
  - [マイグレーションでDBにテーブルを定義する流れのまとめ](#マイグレーションでdbにテーブルを定義する流れのまとめ)
  - [マイグレーションファイルからModelの生成](#マイグレーションファイルからmodelの生成)
  - [Modelの利用方法](#modelの利用方法)
- [サービスコンテナ](#サービスコンテナ)

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


## データにアクセス
複数人で開発を行う場合、開発環境のDBは開発者で管理することになる。
DBの初期テーブルの作成や、変更時に全員で同期する必要がある場合、
DB担当者がSQLを配布することになる。
しかし、この手順は面倒な上、作業漏れが発生する可能性があるため、
マイグレーションを利用する。


マイグレーションファイルからDBを作成する。
反対に、既存のDBからマイグレーションファイルを作成することもできる。

`Laravel-migrations-generator`というツールでマイグレーションファイルを自動生成することができる。
マイグレーションツールをダウンロードするためには、以下のコマンドを実行する。
```
composer require --dev "kitloong/laravel-migrations-generator"
```

## DBの設定
Laravelでは、DBへのアクセス設定は`.env`ファイルに記述されている。

## マイグレーションファイルの生成
マイグレーションファイル関連は以下の公式ドキュメントもある。
- https://readouble.com/laravel/8.x/ja/migrations.html

### 空のマイグレーションファイルの作成
`[マイグレーションファイル名]`で指定したマイグレーションファイルを生成し、
`--create`で生成するテーブル名を指定する。
```
php artisan make:migration [マイグレーションファイル名] --create=[テーブル名]
```

例えば、
```
php artisan make:migration create-books-table --create=books
```

とすると、`/database/migration`以下にマイグレーションファイルが生成される。
このコマンドで生成されたマイグレーションファイルは、次のように生成される。
```php:/database/migration/yyyy_mm_dd_tttttt_create-books-table.php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('books', function (Blueprint $table) {
            $table->id();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('books');
    }
};
```

## マイグレーションファイルからテーブルを作成
例えば、`books`テーブルを以下のようなカラムの設定にしたいとする。
|  カラム名  |  型  |  備考  |
| :----  | :---- | :---- |
|id| int | 連番のIDで、自動採番される。 |
|name| varchar | 書籍名 |
|author| varchar | 著者名 |
|overview| text | 書籍の概要 |


この時、`up()`の処理を次のように変更する。
```php:/database/migration/yyyy_mm_dd_tttttt_create-books-table.php
public function up()
{
    Schema::create('books', function (Blueprint $table) {
        $table->id();
        $table->string('name', 255);
        $table->string('author', 255);
        $table->text('overview');
        $table->timestamps();
    });
}
```

`Blueprint`が持つメソッドは、https://readouble.com/laravel/8.x/ja/migrations.html の
`利用可能なカラムタイプ`の項目で詳細が記載されている。

マイグレーションファイルからテーブルを作成するためには、次のコマンドを実行する。
```
php artisan migrate
```

これで、各種テーブルがDB内に生成されている。


## マイグレーションでDBにテーブルを定義する流れのまとめ
- phpMyadmin(http://localhost:8080/phpmyadmin)にログインし、文字コードを`utf8`で`作成したいデータべース名`でデータベースを作成する。
- `php artisan make:migration`コマンドでマイグレーションファイルを生成する。
- マイグレーションファイルを編集して、テーブルのカラム定義を行う。
- `.env`ファイルにデータベースへの接続設定を記述する。
- `php artisan migration`コマンドでマイグレーションを実施する。


## マイグレーションファイルからModelの生成
以下のコマンドを実行する。
```
php artisan make:model モデル名
```

例えば、`Book`というModelを作成したい場合は、
```
php artisan make:model Book
```

とすると、`/app/Models/Book.php`というファイルが自動生成される。
生成されたModelファイルは、編集せずこのまま用いる。

## Modelの利用方法

```php:IndexController.php
namespace App\Http\Controllers;
use App\Models\Book; // 追記
class IndexController extends Controller
{
    public function index() {
        // Bookの情報を全件取得する。
        $books = Book::all(); // 追記
        return view('index', compact('books'));
    }
}
```

`compact()`では、`'books'`というキーで`$books`を連想配列にしている。

次に、blade側では、次のように実装して、$booksの情報を表示する。
```php:index.blade.php
<div id="mainContent">
  @foreach($books as $book)
  {{ $book->name }}<br />
  {{ $book->author }}<br />
  {{ $book->overview }}<br />
  @endforeach
</div>
```

bladeのリファレンスは以下を参照する。
- https://readouble.com/laravel/10.x/ja/blade.html


# サービスコンテナ
サービスコンテナにクラスを登録するためには、ServiceProviderに登録するクラスの情報を
記述する必要がる。
`*ServiceProvider`クラスは自作することもでき、`php artisan`で生成できる。

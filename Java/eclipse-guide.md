# Eclipse操作ガイド
Eclipseで最低限知っておきたい機能・用語・ショートカットを備忘録として残しておく。

# Javaエディターの基本操作
## コードアシスト
コードアシストは、コードの入力途中で次の入力候補をリストから選択できる機能である。<br>
コードを入力中に`.`を入力すると入力候補のリスト(コンテンツ・アシストと呼ぶ)が表示されるが、`Ctrl + Space`でコンテンツ・アシストを出力することができる。

## ズームイン・ズームアウト
エディター内の文字を大きくしたり、小さくしたりすることができる。
ズームイン: `Ctrl Shift +`<br>
ズームアウト: `Ctrl -`

## テンプレート機能
Eclipseではコードアシストの他にもコード補完が可能な、「テンプレート」という機能がある。<br>
Eclipseのメニューの`ウィンドウ > 設定`から`Java > エディター > テンプレート`と進むと、テンプレートの一覧が表示される。<br>
使用する場合は、テンプレートに登録されているコマンドをエディター上で入力して、`Ctrl Space`を押下する。
例えば、`sysout`と入力して`Ctrl Space`を押下すると、`System.out.println();`が生成される。

## その他のショートカットキー
<table border="1">
<tr>
  <th style="text-align:center;">コマンド</th>
  <th style="text-align:center;">説明</th>
</tr>
<tr>
  <td>Ctrl + D</td>
  <td>選択されている行を削除</td>
</tr>
<tr>
  <td>Ctrl + /</td>
  <td>コメントアウト</td>
</tr>
<tr>
  <td>Ctrl + Shift + ＋</td>
  <td>エディター内の文字を拡大する。(ズームイン)</td>
</tr>
<tr>
  <td>Ctrl + -</td>
  <td>エディター内の文字を縮小する。(ズームアウト)</td>
</tr>
<tr>
  <td>Ctrl + Shift + O</td>
  <td>import宣言が必要なクラスが利用されている場合、import文を生成する。</td>
</tr>
<tr>
  <td>Ctrl + Shift + F</td>
  <td>インデントの調整</td>
</tr>
<tr>
  <td>Ctrl + .</td>
  <td>エラー箇所へジャンプ。再び入力すると次のエラー箇所へジャンプする。</td>
</tr>
<tr>
<tr>
  <td>Ctrl + K</td>
  <td>特定の文字列を選択した後に、このコマンドを実行すると、同じ文字列の箇所へジャンプできる。</td>
</tr>
<tr>
  <td>Ctrl + L</td>
  <td>指定した行番号の行にジャンプ</td>
</tr>
<tr>
  <td>Ctrl + T</td>
  <td>メソッドやクラスを選択して、このコマンドを利用すると、型階層を確認できる。</td>
</tr>
  <td>Alt + →or←</td>
  <td>エディター上に複数のファイルが表示されている場合、アクティブなファイルを切り替える。</td>
</tr>
<tr>
  <td>Ctrl + W</td>
  <td>アクティブなソースファイルを閉じる。</td>
</tr>
<tr>
  <td>Ctrl + Shift + W</td>
  <td>開いているソースファイルを全て閉じる。</td>
</tr>
<tr>
  <td>Alt + Shift + C</td>
  <td>「メソッド・シグニチャの変更」を表示する。メソッドの引数の数や型を変更する際に使う。</td>
</tr>
<!-- <tr>
  <td></td>
  <td></td>
</tr> -->
</table>

# VSCode Eclipse対比表
VSCodeとEclipseで対応するショートカット機能を紹介する。

<table border="1">
<tr>
  <th style="text-align:center;">VSCode</th>
  <th style="text-align:center;">Eclipse</th>
  <th style="text-align:center;">説明</th>
</tr>
<tr>
  <td>Ctrl + Shift + K</td>
  <td>Ctrl + D</td>
  <td>選択されている行を削除</td>
</tr>
<tr>
  <td>Ctrl + /</td>
  <td>Ctrl + /</td>
  <td>コメントアウト</td>
</tr>
<tr>
  <td></td>
  <td></td>
  <td></td>
</tr>
</table>

# Eclipseで使えるビルドツール
- Ant(ビルドファイル(build.xml)をXMLで記述する。)
- Maven(Antの後継で、Maven以降のビルドツールでは、必要なライブラリを自動で入手できるようになった。)
- Gradle(ビルドファイルをGroovyと呼ばれるスクリプト言語で記述する。Javaの他、Android開発でも使われている。)

## Antでのビルド方法

## Mavenでのビルド方法

## Gradleでのビルド方法

# その他のTips
## 半角空白文字が「ᴜ」になっているのを「·」に戻す。
`{Eclipseをダウンロードしたディレクトリ}\dropins\MergeDoc\eclipse\plugins\jp.sourceforge.mergedoc.pleiades\conf`にある`pleiades-config.xml`の次の箇所を修正する。<br>
修正前
```
$3	= $3.equals("·" ) ? "ᴜ"	// 半角空白 例：▫ᵁᐡᶸᓑᴜՍ
```

修正後
```
$3	= $3.equals("" ) ? "ᴜ"	// 半角空白 例：▫ᵁᐡᶸᓑᴜՍ
```

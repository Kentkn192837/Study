# SpringでのController
- `@Controller`アノテーションを付与することで、Controllerクラスとして認識される。
- `Service`クラスを利用する場合は、Serviceインターフェースをメンバー変数に定義し、`@Autowired`アノテーションを付与する。

# Handlerメソッド(@RequestMapping)
- リクエストに応じて実行される処理(業務処理呼び出し, View連携, 画面遷移)を行う際に呼び出す。
- `Handler`メソッドには`@RequestMapping`アノテーションを付与する。
- `@RequestMapping`はリクエストに対して、どのControllerクラスやHandleメソッドを呼び出すかをマッピングするための仕組みである。
- どのような条件でリクエストを絞り込むのかを属性で判別する。

<table border="1">
<tr>
  <th style="text-align:center;">属性</th>
  <th style="text-align:center;">説明</th>
  <th style="text-align:center;">例</th>
</tr>
<tr>
  <td>value<br>path</td>
  <td>URLパスへのマッピングを行う</td>
  <td></td>
</tr>
<tr>
  <td>method</td>
  <td>HTTPメソッドへのマッピングを行う</td>
  <td></td>
</tr>
<tr>
  <td>params</td>
  <td>リクエストに含まれるパラメータ名にマッピングする</td>
  <td></td>
</tr>
</table>

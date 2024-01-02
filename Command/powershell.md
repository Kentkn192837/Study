# Windows PowerShell ISE
PowerShellのコマンド実行、テスト、デバッグの他、PowerShellを利用する際の補助機能を利用できる。

# PowerShell開発のためのVSCodeプラグイン情報
- https://qiita.com/84zume/items/3a4b850f62b6b2636624

# 「このシステムではスクリプトの実行が無効になっているため、...を読み込むことができません」の対処方法
本件は、公式ドキュメントの「PowerShellを使用した実行ポリシーの管理」に詳細が記述されている。
<br>
- https://learn.microsoft.com/ja-jp/powershell/module/microsoft.powershell.core/about/about_execution_policies?view=powershell-7.4

## 実行ポリシーの取得
```
> Get-ExecutionPolicy -List

        Scope ExecutionPolicy
        ----- ---------------
MachinePolicy       Undefined
   UserPolicy       Undefined
      Process       Undefined
  CurrentUser       Undefined
 LocalMachine       Undefined
```

結論、`.ps1`を実行するためには、「実行ポリシー」を変更する必要がある。

## 実行ポリシーを変更する方法
PowerShell 実行ポリシーを変更するには、`Set-ExecutionPolicy`コマンドレットを使用する。
```
> Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

`-ExecutionPolicy`には`AllSigned`, `Bypass`, `Default`, `Restricted`, `Undefined`, `RemoteSigned`,`Unrestricted`から実行ポリシーを選択して指定する。<br>
`-Scope`は実行ポリシーを適用するユーザのスコープで、`MachinePolicy`, `UserPolicy`, `Process`,`CurrentUser`, `LocalMachine`


### RemoteSigned
- Windows サーバー コンピューターに対する既定の実行ポリシー。
- スクリプトは実行できます。
- メールやインスタント メッセージ プログラムを含むインターネットからダウンロードされるスクリプトや構成ファイルには、信頼できる発行者からのデジタル署名が必要です。
- インターネットからダウンロードされたものではなく、ローカル コンピューターに書かれたスクリプトにはデジタル署名は必要ありません。
- Unblock-File コマンドレットを使用するなどしてスクリプトのブロックが解除された場合、インターネットからダウンロードされた符号なしのスクリプトを実行します。
- インターネット以外からの符号なしのスクリプトや、悪意のある可能性のある符号のあるスクリプトを実行するリスク。

その他の実行ポリシー(`AllSigned`, `Bypass`, `Default`, `Restricted`, `Undefined`, `Unrestricted`)については、以下を参照すること。<br>
- https://learn.microsoft.com/ja-jp/powershell/module/microsoft.powershell.core/about/about_execution_policies?view=powershell-7.4#powershell-execution-policies


## 実行ポリシーの削除
実行ポリシーを削除すると、削除を実行したスコープのユーザの実行ポリシーは、`Undefined`に設定される。
詳細な記述は、以下のリンクを参照。<br>
https://learn.microsoft.com/ja-jp/powershell/module/microsoft.powershell.core/about/about_execution_policies?view=powershell-7.4#remove-the-execution-policy


# PowerShell単語
## プロバイダー

# PowerShellのコマンドレットとエイリアス(alias)
## ファイルを作成する(New-Item)
`New-Item`でファイルを作成する。
```
> New-Item test.txt
```

`test.txt`という名前の空のファイルが生成される。

## オブジェクトのメンバーを調べる
`Get-Member`コマンドレットに送られてきたオブジェクトのプロパティやメソッドを調べる。
```
> Get-Process | Get-Member
```

## アクセス可能なPSDriveの一覧を調べる
```
> Get-PSDrive
```

`Function`ドライブでは関数が保存されている。

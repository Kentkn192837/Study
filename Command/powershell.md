# Windows PowerShell ISE
PowerShellのコマンド実行、テスト、デバッグの他、PowerShellを利用する際の補助機能を利用できる。

# PowerShell開発のためのVSCodeプラグイン情報
- https://qiita.com/84zume/items/3a4b850f62b6b2636624

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

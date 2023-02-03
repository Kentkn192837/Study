# アンダースコア2つの変数
## __file__
`__file__`実行中のファイルのパスを取得する。


# Pythonの可変長引数(*args, **kwargs)
`*args`, `**kwargs`という名前は慣例として使われることが多いというだけで、他の名前でも構わない。

## *args: 複数の引数をタプルとして受け取る
## **kwargs: 複数の引数を辞書として受け取る


# Pythonでyml(ヤムル)ファイルを読み込む手順
`yaml`ライブラリをインポートする。
```yaml:config.yml
hyperparameterA:
    name: 'ParamA'
    param1: 1
    param2: 2

hyperparameterB:
    name: 'ParamB'
    param1: 3
    param2: 4
```

のような`config.yml`があるとすると、

```python:test.py
import yaml

with open('config.yml', 'r') as yml:
    config = yaml.load(yml, Loader=yaml.SafeLoader)

print(f"name: {config['hyperparameterA']['name']}")
print(f"name: {config['hyperparameterA']['param1']}")
print(f"name: {config['hyperparameterA']['param2']}")

print(f"name: {config['hyperparameterB']['name']}")
print(f"name: {config['hyperparameterB']['param1']}")
print(f"name: {config['hyperparameterB']['param2']}")

```

のようにアクセスする。

# 複数行に渡る標準出力を上書きしながら出力する方法
`print`で出力する文字列の最初か最後に`\r`を結合して、引数endに空白をしていする。

```python
import time

for i in range(10):
    print(f"{i} 回目\r", end=' ')
    time.sleep(1)
print("完了")
```

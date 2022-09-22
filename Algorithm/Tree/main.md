# 木構造をプログラムで表す方法
- (参考)https://qiita.com/Yuya-Shimizu/items/f2bcd73f2b4d68210820

木構造をリストで表すためには、リストに対して木構造の各ノードをリストのインデックス番号に見立てて考え、値には分岐先のインデックス番号を格納する。(連結リスト表現)

```python:main.py
def main():
    data = [[1, 2],
            [3, 4], [5, 6],
            [7, 8], [9, 10], [11, 12], [13, 14],
            [], [], [], [], [], [], []]

if __name__ == '__main__':
    main()
```

## 幅優先探索でデータを取り出す

- 基本の幅優先探索例

```python:bfs.py
def bfs(data):
    # キューを探索を開始するノードのインデックス番号で初期化
    queue = [0]

    while len(queue) > 0:
        pos = queue.pop(0)      # 探索するノードのインデックス番号を取得
        print(pos, end=' ')
        for i in data[pos]:
            queue.append(i)     # 探索したノードから分岐している枝の情報を取得
```

## 深さ優先探索でデータを取り出す

- 基本の深さ優先探索例

```python:dfs.py
def dfs(pos):
    print(pos, end=' ')
    for i in data[pos]:
        dfs(i)

# 最初に探索するノードを0に指定
dfs(0)
```

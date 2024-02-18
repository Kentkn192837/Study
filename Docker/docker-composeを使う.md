# docker-composeでできること
# Dockerfileとdocker-composeの役割の違い
## Dockerfile
- イメージをどのように作成するかを定義できる。

## docker-compose
- イメージをどのように展開するかを定義できる。
- 複数コンテナで構成されるアプリの設計。
- コンテナのライフサイクル(ビルド/停止/起動)をDockerに管理させることができる。

結論としては、`Dockerfile`でイメージ単体の開発コストを下げる、
`docker-compose`でアプリ全体としての開発コストと運用コストを大幅に下げられる。
composeの構成ファイルが正しければ、人的ミスによるコンテナの間違った運用も発生しにくく、
トラブル防止になる。
きちんとDockerを開発/運用で利用するのであれば、composeはほぼ必須と言える。

`docker-compose`は、複数コンテナによる構成で強みを発揮するが、
シンプルな構成であっても、デプロイ作業を定義通りに実行できるので便利である。


複数のコンテナをまとめて管理することを「オーケストレーション」と呼ぶ。

# docker-composeファイルを開発用と本番用でそれぞれ用意する
`docker-compose`でコンテナを起動する場合は、`docker-compose up -d`コマンドを使う。
これによって、`docker-compose.yml`ファイルからコンテナが起動される。
しかし、`-f`オプションを使うことで、任意の名前の`docker-compose`ファイルからコンテナの起動ができる。

```
$ docker-compose -f docker-compose.local.yml up -d
```

`--build`オプションを付けて実行すると、コンテナ起動時に必ずビルド処理を実行する。

## 開発環境・本番環境別のdocker-composeファイルの命名作法
- docker-compose.local.yml (開発用)
- docker-compose.prod.yml (本番用)

## 開発環境用と本番環境用のdocker-composeファイルの作成方法
`docker-compose.prod.yml`では以下の点を守ること。
- `image:`でデフォルトのイメージ名ではなく、作成されるイメージ名を指定する。
- `container:`でデフォルトのコンテナ名ではなく、作成されるコンテナ名を指定する。
- `restart: unless-stopped`とすることで、意図せず停止した際に再起動するようにする。
- `voluems:`でホストコンピュータのディレクトリとコンテナのディレクトリをバインドしない。

デフォルトのイメージ名ではなく、作成されるイメージ名を指定することで、Compose外の利用(push)をしやすくなる。
コンテナ名を指定することで、dockerコマンドで識別しやすくする。


# Composeでできるその他の機能
## ComposeでリモートのDockerホストを操作する
`docker-compose`コマンドの`-H`オプションで接続先のホストPCでコマンドを実行できる。
sshで接続するためには、`ssh-keygen`で作成した公開鍵をホストPCに登録する必要がある。

このように、リモートのDockerホストに対してComposeでアプリを展開する方法は便利で、
大量のDockerホストがある環境でアプリを展開する際に、コントローラのような1つのマシンにコードを集約し、
そこを起点にリモートDockerホストにさまざまな構成をたくさん作ることができる。

## Composeファイル内で環境変数を参照する
```yml: docker-compose.yml
version: '3.7'
services:
  web:
    image: ${DHUB_USER}/c5env:${IMG_VER}
    container_name: c5env
```

上記の`docker-compose.yml`では、イメージの指定部分で、`${DHUB_USER}`と`${IMG_VER}`が使われている。
そして、以上の`docker-compose.yml`ファイルに対して、このdocker-composeファイルと同じ階層に`.env`ファイルを定義すると、`docker-compose.yml`ファイル内に定義した埋め込み変数に`.env`に定義した環境変数が埋め込まれる。

```.env:
DHUB_USER=superuser110
IMG_VER=v1.0
```

このようにすることで、シェルの環境変数を使うように、
`${DHUB_USER}/c5env:${IMG_VER}`の部分が、`superuser110/c5env:v1.0`と解釈される。

`.env`ファイルは、開発者個人が個々に持てるように、`.gitignore`でバージョン管理から外しておくのが、一般的な運用である。


## その他のComposeの命令
// TODO:tty, env_fileなどの説明を書く


# Composeで本格的な3階層アプリを開発方法

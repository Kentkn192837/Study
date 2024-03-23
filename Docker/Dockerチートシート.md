## Dockerfileチートシート
- Dockerfileによるビルド時間を短縮するためには、頻繁に変更が発生する箇所をDockerfileの後半に記述する。(p.123)
- `.dockerignore`をDockerfileが存在するディレクトリに配置しておくと、DockerfileのCOPY命令など対象から、`.dockerignore`に記述した形式のファイルが除外される。
```.dockerignore:
**/__pycache__
**/.DS_Store
**/Thumbs.db    # Windowsでのキャッシュデータ
```

### Dockerfileを開発用と本番用で分けたい
`docker image build`に`-f`オプションを指定することで、任意の名前のファイルを`Dockerfile`として扱うことができる。
例えば、`Dockerfile.local.yml`を`Dockerfile`として扱う場合は、次のコマンドでビルドすれば良い。
```
$ docker image build -f Dockerfile.local.yml -t mynginx ./
```

開発用: `Dockerfile.local.yml`<br>
本番用: `Dockerfile.prod.yml`


### Dockerfileでステージングビルドをしたい
ステージングビルドとは、C言語やJavaなど、コンパイルを実施して、ソースコードをバイナリ形式の実行ファイルに変換する処理が必要な
言語を扱う際に、コンパイル用のイメージと本番環境で実行ファイルを実行するためのイメージを分けることができる。
これによって、イメージを軽量化が期待できる。
```Dockerfile:
# ビルド用イメージ
FROM golang:1.13.4-alpine3.10 as builder
WORKDIR /src
COPY ./main.go /src
RUN go build -o start_appserver main.go

# 実行用イメージ
FROM alpine:3.10.3
COPY --from=builder /src/start_appserver /bin/start_appserver
CMD ["/bin/start_appserver"]
```

`as builder`としてビルド用イメージに名前を与え、`--from=builder`として、Dockerホストではなく、
1つ目のイメージで作成したバイナリファイルをイメージ内部にコピーしている。

このDockerfileのビルドと実行手順の例↓
```
$ docker image build ./ -t c4app4
$ docker container run --rm -d -p 8080:80 --name myapp c4app4
```

#### バイナリアプリのみのコンテナの作り方(非推奨)
ScratchというすべてのDockerイメージの原点となるイメージにバイナリファイルを置くと、busyboxを超える超軽量イメージを作成する
ことができる。ただし、Linuxの機能が使えず、トラブル発生時の調査が面倒となることが多いため、
積極的な利用は推奨されない。
```Dockerfile:
# ビルド用イメージ
FROM golang:1.13.4-alpine3.10 as builder
WORKDIR /src
COPY ./main.go /src
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -installsuffix cgo -o startapp main.go

# 実行用イメージ
FROM scratch
COPY --from=builder /src/startapp /startapp
CMD ["/startapp"]
```

## docker-composeチートシート
リファレンスはここ
- https://docs.docker.jp/compose/compose-file/compose-file-v3.html

### services
#### restart
- https://matsuand.github.io/docs.docker.jp.onthefly/config/containers/start-containers-automatically/

`restart: unless-stopped`, `restart: always`などと宣言する。
どちらもコンテナが異常停止した場合に再起動させるための設定だが、`unless-stopped`では、手動でコンテナを停止させたときでも
再起動の処理が為される。

#### depends_on
コンテナ間の依存関係を定義する。
以下のような`docker-compose.yml`を例にすると、
```docker-compose.yml:
services:
    mysql:
        image: mysql:5.7.28
        volumes:
        - mysql_volume:/var/lib/mysql
        networks:
        - wp_net
        ...

    nodejs:
        image: nodejs
        networks:
        - wp_net
        depends_on: mysql
        ...
        ...

networks:
    wp_net:
        driver: bridge

volumes:
    mysql_volume:
        driver: local
```

`mysql`コンテナが立ち上がってから`nodejs`コンテナを立ち上げるという処理が為される。

### networks
- https://docs.docker.jp/compose/networking.html

### volumes
- https://knowledge.sakura.ad.jp/26522/

### docker-composeの起動方法
```
(基本)docker-compose up -d
(コマンド実行時に必ずビルドする)docker-compose up -d --build
```

### アプリケーションをホットリロードしたい
- `Docker Compose Watch`というツールがある。
- https://zenn.dev/maybe_dog/articles/bfeeee3b6650a1

### docker-composeを開発用と本番用で分けたい
以下のような役割の`docker-compose`ファイルをそれぞれ用意する。
```
(環境構築用)docker-compose.dev.yml
(開発用)docker-compose.local.yml
(検証用)docker-compose.staging.yml
(テスト用)docker-compose.test.yml
(本番用)docker-compose.production.yml
```

`-f`オプションでコンテナを展開する`docker-compose`ファイルを指定する
```
(基本)docker-compose -f docker-compose.local.yml up -d
(コマンド実行時に必ずビルドする)docker-compose -f docker-compose.local.yml up -d --build
```

頻繁に使うコマンドはシェルスクリプトにしたり、`Makefile`にしたりすると便利。
```Makefile:
SHELL=/bin/bash

build:
	docker-compose -f docker-compose.local.yml build

up:
	docker-compose -f docker-compose.local.yml up -d

stop:
	docker-compose -f docker-compose.local.yml stop

down:
	docker-compose -f docker-compose.local.yml down

ps:
	docker ps
```

### ComposeでリモートのDockerホストを操作したい
### Composeファイル内で環境変数を参照したい
`docker-compose`ファイルと`.env`ファイルを同じ階層に配置する。
```yml: docker-compose.yml
version: '3.7'
services:
  web:
    image: ${DHUB_USER}/c5env:${IMG_VER}
    container_name: c5env
```

```.env:
DHUB_USER=superuser110
IMG_VER=v1.0
```

このようにすることで、シェルの環境変数を使うように、
`${DHUB_USER}/c5env:${IMG_VER}`の部分が、`superuser110/c5env:v1.0`と解釈される。

`.env`ファイルは、開発者個人が個々に持てるように、`.gitignore`でバージョン管理から外しておくのが、一般的な運用である。

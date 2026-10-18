# S3
## バケットの作成
```sh
aws s3 mb s3://awsinaction-test
```

## バケットと手元のPCの同期
手元のPCの`/Users/andreas/Desktop` というフォルダと、`s3://awsinaction-test/backup` を比較して、新しいファイルか、変更されたファイルだけをアップロードする。
```sh
aws s3 sync /Users/andreas/Desktop s3://awsinaction-test/backup
```

## バケットの削除
バケット内のオブジェクトを削除した上で、さらにバケット自体を削除する。バージョニングを有効にしていると`BacketNotEmpty`エラーになる。
```sh
aws s3 rb --force s3://awsinaction-test
```

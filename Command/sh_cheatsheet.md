# .shファイルを直接実行する方法
```:hello.sh
echo Hello.
```

`.sh`は、`chmod`で実行権限を与えることでコマンドで`.sh`ファイルを実行することができる。

```
$ chmod a+x hello.sh
$ ./hello.sh
Hello.
```

# .shファイルをシェル以外からも実行する方法
シバン(シェバン:shebang)という`#!`で始まる1行を先頭に追加する。
```:hello.sh
#!/bin/bash
echo Hello.
```

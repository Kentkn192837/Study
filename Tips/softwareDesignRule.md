大原則:データとそのデータを操作するロジックは同じクラスで扱う。

### 高凝集と低凝集

# 低凝集を回避するための設計
## staticメソッドの扱い方
- staticメソッドは`ログ出力用メソッド`や`フォーマット変換用`メソッドなど、凝集度に影響がない処理に利用する。
- ファクトリメソッドとして利用する。
- 横断的関心事はstaticメソッドで良い。

### ファクトリメソッド
コンストラクタを`private`にして、クラス内部のみでインスタンス生成する形にする。

```java:Giftpoint.java
class GiftPoint {
    private static final int MIN_POINT = 0;
    private static final int STANDARD_MEMBERSHIP_POINT = 3000;  // 標準会員向けギフトポイント
    private static final int PREMIUM_MEMBERSHIP_POINT = 10000;  // プレミアム会員向けギフトポイント
    final int value;

    private GiftPoint(final int point) {
        if (point < MIN_POINT) {
            throw new IllegalArgumentException("ポイントが0以上ではありません。");
        }
        this.value = point;
    }

    static GiftPoint forStandardMembership() {
        return new GiftPoint(STANDARD_MEMBERSHIP_POINT);
    }

    static GiftPoint forPremiumMembership() {
        return new GiftPoint(PREMIUM_MEMBERSHIP_POINT);
    }
    :
}
```

### staticメソッドの見分け方

### Common, Utilのような共通処理クラスが表れたら注意
再利用性はCommonやUtilのようなクラスにまとめて設計するのではなく、再利用性は高凝集な設計にすることで高まる。

### 結果を返すために引数は使わないこと


### 引数が多すぎる場合は注意
引数の量が多いということは、それだけ処理させたい内容が膨らむことになる。こうした状況を生み出す原因の一つとして、基本データ型(プリミティブ型)執着がある。引数が多すぎる事態に陥らないためには、概念的に意味のあるクラスをつくることが肝要である。

### staticメソッドとして設計して良い例
#### 横断的関心事の例
- ログ出力
- エラー検出
- デバッグ
- 例外処理
- キャッシュ
- 同期処理
- 分散処理

### メソッドチェインを回避すること
インスタンス変数を`private`にして、外部からアクセスできなくする。インスタンス変数に対する制御は、メソッドとして外部から命じる形にする。詳細なロジックは呼ぶ側ではなく、呼ばれる側に実装する。


# 条件分岐を減らす方法
##


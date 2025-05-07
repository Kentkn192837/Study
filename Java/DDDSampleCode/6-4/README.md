# interfaceの利用でinstanceofによる型判定を排除
```java
Money busySeasonFee;
if (hotelRates instanceof RegularRates) {
    busySeasonFee = hotelRates.fee().add(new Money(3000));
} else if (hotelRates instanceof PremiumRates) {
    busySeasonFee = hotelRates.fee().add(new Money(5000));
}
```

プログラム例では、instanceofを使って料金切り替え処理を実装してしまっていて、せっかくinterfaceを使っているのに
他にも繁忙期料金を使いたいロジックがある場合、同じような条件分岐をまた書かなければならない。

**リスコフの置換原則**によると、クラスの基本型を継承型に置き換えても問題なく動作しなければならない。ここでは、
基本型はinterfaceで、継承型はinterfaceの実装クラスとなる。

繫忙期料金を返すメソッドもinterfaceに追加することでinstanceofでの型判定を削除します。

①if/elseで処理のふるまいを変えている<br>
②やることが同じでやり方が違うような場合<br>
は、ストラテジパターンが適用できる可能性が高い。

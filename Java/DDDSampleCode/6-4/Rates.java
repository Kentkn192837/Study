interface HotelRates {
    Money fee();

    Money busySeasonFee(); // 繁忙期の料金
}

/**
 * 通常宿泊料金に繁忙期料金を追加
 */
class RegularRates implements HotelRates {
    public Money fee() {
        return new Money(7000);
    }

    public Money busySeasonFee() {
        return fee().add(new Money(3000));
    }
}

/**
 * プレミアム宿泊料金に繁忙期の宿泊料金
 */
class PremiumRates implements HotelRates {
    public Money fee() {
        return new Money(12000);
    }

    public Money busySeasonFee() {
        return fee().add(new Money(5000));
    }
}

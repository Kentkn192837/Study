class GoldCustomerPurchaseAmountRule implements ExcellentCustomerRule {
    /**
     * ゴールド会員の購入金額ルール
     * これまでの購入金額が100,000円以上であること
     */
    @Override
    public boolean ok(final PurchaseHistory history) {
        return 100000 <= history.totalAmount;
    }
}

class PurchaseFrequencyRule implements ExcellentCustomerRule {
    /**
     * 購入頻度ルール
     * これまでの購入の頻度が一ヶ月あたり10回以上であること
     */
    @Override
    public boolean ok(final PurchaseHistory history) {
        return 10 <= history.PurchaseFrequencyPerMonth;
    }
}

class ReturnRateRule implements ExcellentCustomerRule {
    /**
     * 返品率ルール
     * 返品率が0.1%以内であること
     */
    @Override
    public boolean ok(final PurchaseHistory history) {
        return history.returnRate <= 0.001;
    }
}

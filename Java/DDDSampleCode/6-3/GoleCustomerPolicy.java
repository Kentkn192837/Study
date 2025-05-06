class GoldCustomerPolicy {
    private final ExcellentCustomerPolicy policy;

    GoldCustomerPolicy() {
        policy = new ExcellentCustomerPolicy();
        policy.add(new GoldCustomerPurchaseAmountRule());
        policy.add(new PurchaseFrequencyRule());
        policy.add(new ReturnRateRule());
    }

    /**
     * @param history 購入履歴
     * @return ルールをすべて満たす場合はtrue
     */
    boolean complyWithAll(final PurchaseHistory history) {
        return policy.complyWithAll(history);
    }
}

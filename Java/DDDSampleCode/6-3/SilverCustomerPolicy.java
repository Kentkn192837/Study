class SliverCustomerPolicy {
    private final ExcellentCustomerPolicy policy;

    SilverCustomerPolicy() {
        policy = new ExcellentCustomerPolicy();
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

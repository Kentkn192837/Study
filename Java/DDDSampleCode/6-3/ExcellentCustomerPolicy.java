import java.util.HashSet;
import java.util.Set;

class ExcellentCustomerPolicy implements CustomerPolicy {
    private final Set<ExcellentCustomerRuleIF> rules;

    public ExcellentCustomerPolicy() {
        rules = new HashSet<>();
    }

    /**
     * ルールを追加する
     * 
     * @param rule ルール
     */
    void add(final ExcellentCustomerRuleIF rule) {
        rules.add(rule);
    }

    /**
     * @param history 購入履歴
     * @return ルールをすべて満たす場合はtrue
     */
    boolean complyWithAll(final PurchaseHistory history) {
        for (ExcellentCustomerRuleIF each : rules) {
            if (!each.ok(history)) {
                return false;
            }
        }
        return true;
    }
}

public class ExecuteService {
    enum DamageType {
        hitPoint,
        magicPoint
    }

    private final Map<DamageType, Damage> damages;

    void applyDamage(final DamageType damageType, final int damageAmount) {
        final Damage damage = damages.get(damageType);
        damage.execute(damageAmount);
    }

    void execute() {
        int damageAmount = 10;
        applyDamage(DamageType.magicPoint, damageAmount);
    }
}

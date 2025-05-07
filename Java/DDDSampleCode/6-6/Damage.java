interface Damage {
    void execute(final int damageAmount);
}

class HitPointDamage implements Damage {
    // 中略
    @Override
    public void execute(final int damageAmount) {
        // ダメージを与える処理
        member.hitPoint -= damageAmount;
        if (0 < member.hitPoint) {
            return;
        }
        member.hitPoint = 0;
        member.addState(StateType.dead);
    }
}

class MagicPointDamage implements Damage {
    @Override
    public void execute(final int damageAmount) {
        // マジックポイントにダメージを与える処理
        member.magicPoint -= damageAmount;
        if (0 < member.magicPoint) {
            return;
        }
        member.magicPoint = 0;
    }
}

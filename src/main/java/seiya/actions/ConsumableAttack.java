package seiya.actions;

import seiya.characters.Character;

public class ConsumableAttack extends Attack {
    public ConsumableAttack(String name, double spiritCost, int damage) {
        super(name, spiritCost, damage);
    }

    @Override
    public boolean canExecute(Character actor) {
        return super.canExecute(actor) && actor.hasConsumable(this) && actor.isConsumableUnlocked();
    }

    @Override
    public String execute(Character actor, Character target) {
        if (!canExecute(actor)) {
            return actor.name() + " cannot use consumable " + name() + ".";
        }

        return super.execute(actor, target) + " (consumable spent)";
    }

    @Override
    public String executeForClash(Character actor, Character target, double opposingDefenseValue, boolean blockedByDefense) {
        if (!canExecute(actor)) {
            return actor.name() + " cannot use consumable " + name() + ".";
        }

        return super.executeForClash(actor, target, opposingDefenseValue, blockedByDefense) + " (consumable spent)";
    }

    @Override
    protected void afterExecute(Character actor) {
        actor.consume(this);
    }
}

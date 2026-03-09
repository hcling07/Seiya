package seiya.actions;

import seiya.characters.Character;
import seiya.util.NumberFormatter;

public class Attack extends Action {
    private final double spiritCost;

    public Attack(String name, double spiritCost, double value) {
        this(name, spiritCost, value, value);
    }

    public Attack(String name, double spiritCost, double attackValue, double defenseValue) {
        super(name, attackValue, defenseValue);
        this.spiritCost = spiritCost;
    }

    @Override
    public double spiritCost() {
        return spiritCost;
    }

    @Override
    public boolean canExecute(Character actor) {
        return actor.spirit() >= spiritCost;
    }

    @Override
    public String execute(Character actor, Character target) {
        if (!canExecute(actor)) {
            return actor.name() + " does not have enough spirit for " + name() + ".";
        }

        actor.spendSpirit(spiritCost);
        double dealt = target.receiveDamage(attackValue());
        afterExecute(actor);
        return actor.name() + " used " + name() + " and dealt " + NumberFormatter.fmt(dealt) + " damage to " + target.name() + ".";
    }

    public String executeForClash(Character actor, Character target, double opposingDefenseValue, boolean blockedByDefense) {
        if (!canExecute(actor)) {
            return actor.name() + " does not have enough spirit for " + name() + ".";
        }

        actor.spendSpirit(spiritCost);
        double dealt = 0.0;
        if (!blockedByDefense) {
            dealt = target.receiveDamage(attackValue() - opposingDefenseValue);
        }
        afterExecute(actor);

        if (blockedByDefense) {
            return actor.name() + " used " + name() + " but could not break through " + target.name() + "'s defense.";
        }

        return actor.name() + " used " + name() + " and dealt " + NumberFormatter.fmt(dealt) + " damage to " + target.name() + ".";
    }

    protected void afterExecute(Character actor) {
        // Hook for special attack types.
    }
}

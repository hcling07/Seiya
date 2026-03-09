package seiya.actions;

import seiya.characters.Character;
import seiya.util.NumberFormatter;

public class Attack extends Action {
    private final int spiritCost;
    private final double attackValue;
    private final double defenseValue;

    public Attack(String name, int spiritCost, double value) {
        this(name, spiritCost, value, value);
    }

    public Attack(String name, int spiritCost, double attackValue, double defenseValue) {
        super(name);
        this.spiritCost = spiritCost;
        this.attackValue = attackValue;
        this.defenseValue = defenseValue;
    }

    public int spiritCost() {
        return spiritCost;
    }

    public double attackValue() {
        return attackValue;
    }

    public double defenseValue() {
        return defenseValue;
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
        double dealt = target.receiveDamage(attackValue);
        afterExecute(actor);
        return actor.name() + " used " + name() + " and dealt " + NumberFormatter.fmt(dealt) + " damage to " + target.name() + ".";
    }

    public String executeForClash(Character actor, Character target, boolean blockedByDefense) {
        if (!canExecute(actor)) {
            return actor.name() + " does not have enough spirit for " + name() + ".";
        }

        actor.spendSpirit(spiritCost);
        double dealt = 0.0;
        if (!blockedByDefense) {
            dealt = target.receiveDamage(attackValue);
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

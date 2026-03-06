package seiya.actions;

import seiya.characters.Character;

public class Attack extends Action {
    private final int spiritCost;
    private final int damage;

    public Attack(String name, int spiritCost, int damage) {
        super(name);
        this.spiritCost = spiritCost;
        this.damage = damage;
    }

    public int spiritCost() {
        return spiritCost;
    }

    public int damage() {
        return damage;
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
        int dealt = target.receiveDamage(damage);
        return actor.name() + " used " + name() + " and dealt " + dealt + " damage to " + target.name() + ".";
    }
}

package seiya.actions;

import seiya.characters.Character;

public class ConsumableAttack extends Attack {
    public ConsumableAttack(String name, int spiritCost, int damage) {
        super(name, spiritCost, damage);
    }

    @Override
    public boolean canExecute(Character actor) {
        return super.canExecute(actor) && actor.hasConsumable(this);
    }

    @Override
    public String execute(Character actor, Character target) {
        if (!canExecute(actor)) {
            return actor.name() + " cannot use consumable " + name() + ".";
        }

        String result = super.execute(actor, target);
        actor.consume(this);
        return result + " (consumable spent)";
    }
}

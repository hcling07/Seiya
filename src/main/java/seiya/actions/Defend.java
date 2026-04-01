package seiya.actions;

import seiya.characters.Character;

public class Defend extends Action {
    private final int damageReductionPercent;

    public Defend(int damageReductionPercent) {
        this(damageReductionPercent, damageReductionPercent);
    }

    public Defend(int damageReductionPercent, double defenseValue) {
        super("Defend", 0.0, defenseValue);
        this.damageReductionPercent = damageReductionPercent;
    }

    @Override
    public boolean canExecute(Character actor) {
        return true;
    }

    @Override
    public String execute(Character actor, Character target) {
        actor.activateDefense(damageReductionPercent);
        if (damageReductionPercent == 0) {
            return actor.name() + " is defending with defense value "
                + defenseValue() + ".";
        }
        return actor.name() + " is defending and will reduce incoming damage by "
            + damageReductionPercent + "% on the next hit.";
    }
}

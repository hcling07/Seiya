package seiya.actions;

import seiya.characters.Character;

public class Defend extends Action {
    private final int damageReductionPercent;

    public Defend(int damageReductionPercent) {
        super("Defend");
        this.damageReductionPercent = damageReductionPercent;
    }

    @Override
    public boolean canExecute(Character actor) {
        return true;
    }

    @Override
    public String execute(Character actor, Character target) {
        actor.activateDefense(damageReductionPercent);
        return actor.name() + " is defending and will reduce incoming damage by "
            + damageReductionPercent + "% on the next hit.";
    }
}

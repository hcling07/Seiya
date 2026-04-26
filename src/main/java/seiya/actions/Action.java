package seiya.actions;

import seiya.characters.Character;
import seiya.game.Player;

public abstract class Action {
    private final String name;
    private final double attackValue;
    private final double defenseValue;

    protected Action(String name) {
        this(name, 0.0, 0.0);
    }

    protected Action(String name, double attackValue, double defenseValue) {
        this.name = name;
        this.attackValue = attackValue;
        this.defenseValue = defenseValue;
    }

    public final String name() {
        return name;
    }

    public double attackValue() {
        return attackValue;
    }

    public double defenseValue() {
        return defenseValue;
    }

    public double spiritCost() {
        return 0.0;
    }

    public abstract boolean canExecute(Character actor);

    public ActionSuppression suppresses(Player actor, Player target, Action opposingAction) {
        return null;
    }

    public abstract String execute(Character actor, Character target);
}

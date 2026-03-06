package seiya.actions;

import seiya.characters.Character;

public abstract class Action {
    private final String name;

    protected Action(String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }

    public abstract boolean canExecute(Character actor);

    public abstract String execute(Character actor, Character target);
}

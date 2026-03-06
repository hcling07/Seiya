package seiya.actions;

import seiya.characters.Character;

public class Gather extends Action {
    private final int spiritGain;

    public Gather(int spiritGain) {
        super("Gather");
        this.spiritGain = spiritGain;
    }

    @Override
    public boolean canExecute(Character actor) {
        return true;
    }

    @Override
    public String execute(Character actor, Character target) {
        actor.gainSpirit(spiritGain);
        return actor.name() + " gathered " + spiritGain + " spirit.";
    }
}

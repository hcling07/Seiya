package seiya.actions;

import seiya.characters.Character;

public class Gather extends Action {
    private final double spiritGain;

    public Gather(double spiritGain) {
        super("Gather", 0.0, 0.0);
        this.spiritGain = spiritGain;
    }

    public double spiritGain() {
        return spiritGain;
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

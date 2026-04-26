package seiya.actions;

import seiya.characters.Character;

public class WearArmor extends Action {
    private static final double DEFAULT_DEFENSE_VALUE = 3.0;

    public WearArmor() {
        this(DEFAULT_DEFENSE_VALUE);
    }

    public WearArmor(double defenseValue) {
        super("Wear Armor", 0.0, defenseValue);
    }

    public int armorPieces() {
        return 1;
    }

    @Override
    public boolean canExecute(Character actor) {
        return actor.canWearArmor();
    }

    @Override
    public String execute(Character actor, Character target) {
        if (!canExecute(actor)) {
            return actor.name() + " cannot wear more armor.";
        }

        for (int i = 0; i < armorPieces(); i++) {
            actor.wearArmorPiece();
        }
        return actor.name() + " wore one armor piece (" + actor.armorWorn() + "/" + actor.totalArmor() + ").";
    }
}

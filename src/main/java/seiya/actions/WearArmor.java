package seiya.actions;

import seiya.characters.Character;

public class WearArmor extends Action {
    public WearArmor() {
        super("Wear Armor");
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

        actor.wearArmorPiece();
        return actor.name() + " wore one armor piece (" + actor.armorWorn() + "/" + actor.totalArmor() + ").";
    }
}

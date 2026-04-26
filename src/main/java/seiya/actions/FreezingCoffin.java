package seiya.actions;

import seiya.characters.Character;
import seiya.game.Player;
import seiya.util.NumberFormatter;

public class FreezingCoffin extends ConsumableAttack {
    public FreezingCoffin() {
        super("Freezing Coffin", 0.0, 0.0, 5.0);
    }

    @Override
    public String execute(Character actor, Character target) {
        if (!canExecute(actor)) {
            return actor.name() + " cannot use consumable " + name() + ".";
        }

        actor.consume(this);
        return actor.name() + " used " + name() + ".";
    }

    @Override
    public String executeForClash(Character actor, Character target, double opposingDefenseValue, boolean blockedByDefense) {
        return execute(actor, target);
    }

    @Override
    public ActionSuppression suppresses(Player actor, Player target, Action opposingAction) {
        if (!canExecute(actor.character())) {
            return null;
        }

        if (opposingAction instanceof Gather) {
            double stolenSpirit = ((Gather) opposingAction).spiritGain();
            actor.character().gainSpirit(stolenSpirit);
            return new ActionSuppression(this, "stole " + NumberFormatter.fmt(stolenSpirit)
                + " spirit from " + target.name() + "'s Gather.");
        }

        if (opposingAction instanceof WearArmor) {
            int stolenArmor = target.character().removeAvailableArmor(((WearArmor) opposingAction).armorPieces());
            actor.character().addAvailableArmor(stolenArmor);
            return new ActionSuppression(this, "stole " + stolenArmor
                + " available armor from " + target.name() + "'s Wear Armor.");
        }

        if (opposingAction instanceof Attack
            && !(opposingAction instanceof FreezingCoffin)
            && opposingAction.attackValue() <= defenseValue()) {
            if (opposingAction instanceof ConsumableAttack) {
                target.character().consume((ConsumableAttack) opposingAction);
            }
            actor.character().addConsumable(new ConsumableAttack(
                opposingAction.name(),
                0.0,
                opposingAction.attackValue(),
                opposingAction.defenseValue()
            ));
            return new ActionSuppression(this, "sealed " + target.name() + "'s "
                + opposingAction.name() + " as a consumable attack.");
        }

        return null;
    }
}

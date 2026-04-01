package seiya.controllers;

import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
import seiya.game.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BasicAiController implements Controller {
    @Override
    public Action chooseAction(Player self, Player opponent, List<Action> availableActions) {
        Optional<Action> lethal = availableActions.stream()
            .filter(Attack.class::isInstance)
            .map(Attack.class::cast)
            .filter(attack -> opponent.character().wouldBeDefeatedBy(attack.attackValue()))
            .map(Action.class::cast)
            .findFirst();
        if (lethal.isPresent()) {
            return lethal.get();
        }

        Optional<Action> bestAttack = availableActions.stream()
            .filter(Attack.class::isInstance)
            .max(Comparator.comparingDouble(action -> ((Attack) action).attackValue()));
        if (bestAttack.isPresent()) {
            return bestAttack.get();
        }

        Optional<Action> wearArmor = availableActions.stream()
            .filter(WearArmor.class::isInstance)
            .findFirst();
        if (wearArmor.isPresent() && shouldWearArmor(self)) {
            return wearArmor.get();
        }

        return availableActions.stream()
            .filter(Gather.class::isInstance)
            .findFirst()
            .orElse(availableActions.get(0));
    }

    private boolean shouldWearArmor(Player self) {
        if (!self.character().ruleSet().tracksHealth()) {
            return self.character().armorWorn() == 0;
        }
        return self.character().health() <= self.character().maxHealth() / 2;
    }
}

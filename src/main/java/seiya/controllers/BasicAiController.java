package seiya.controllers;

import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
import seiya.controllers.rules.AiDecisionRule;
import seiya.controllers.rules.DefendAgainstLikelyAttackRule;
import seiya.game.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BasicAiController implements Controller {
    private final List<AiDecisionRule> decisionRules;

    public BasicAiController() {
        this(Collections.singletonList(new DefendAgainstLikelyAttackRule()));
    }

    public BasicAiController(List<AiDecisionRule> decisionRules) {
        this.decisionRules = new ArrayList<>(decisionRules);
    }

    @Override
    public Action chooseAction(Player self, Player opponent, List<Action> availableActions) {
        Optional<Action> defensiveAction = chooseRuleAction(self, opponent, availableActions);
        if (shouldPreferDefenseBeforeAttack(self, defensiveAction)) {
            return defensiveAction.get();
        }

        Optional<Action> lethal = availableActions.stream()
            .filter(Attack.class::isInstance)
            .map(Attack.class::cast)
            .filter(attack -> opponent.character().wouldBeDefeatedBy(attack.attackValue()))
            .map(Action.class::cast)
            .findFirst();
        if (lethal.isPresent()) {
            return lethal.get();
        }

        if (defensiveAction.isPresent()) {
            return defensiveAction.get();
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

    private Optional<Action> chooseRuleAction(Player self, Player opponent, List<Action> availableActions) {
        for (AiDecisionRule rule : decisionRules) {
            Optional<Action> action = rule.chooseAction(self, opponent, availableActions);
            if (action.isPresent() && availableActions.contains(action.get())) {
                return action;
            }
        }
        return Optional.empty();
    }

    private boolean shouldPreferDefenseBeforeAttack(Player self, Optional<Action> defensiveAction) {
        return defensiveAction.isPresent()
            && !self.character().ruleSet().tracksHealth()
            && defensiveAction.get() instanceof WearArmor;
    }

    private boolean shouldWearArmor(Player self) {
        if (!self.character().ruleSet().tracksHealth()) {
            return self.character().armorWorn() == 0;
        }
        return self.character().health() <= self.character().maxHealth() / 2;
    }
}

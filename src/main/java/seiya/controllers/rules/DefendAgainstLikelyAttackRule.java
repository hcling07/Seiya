package seiya.controllers.rules;

import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.actions.Defend;
import seiya.actions.WearArmor;
import seiya.game.Player;

import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;

public class DefendAgainstLikelyAttackRule implements AiDecisionRule {
    private final DoubleSupplier probabilityRoll;

    public DefendAgainstLikelyAttackRule() {
        this(Math::random);
    }

    public DefendAgainstLikelyAttackRule(DoubleSupplier probabilityRoll) {
        this.probabilityRoll = probabilityRoll;
    }

    @Override
    public Optional<Action> chooseAction(Player self, Player opponent, List<Action> availableActions) {
        if (self.character().defendPercent() > 0) {
            return Optional.empty();
        }

        double opponentAttackLikelihood = opponentAttackLikelihood(opponent);
        if (opponentAttackLikelihood <= 0.0) {
            return Optional.empty();
        }

        if (probabilityRoll() >= opponentAttackLikelihood) {
            return Optional.empty();
        }

        Optional<Action> defend = findAction(availableActions, Defend.class);
        if (!defend.isPresent()) {
            return Optional.empty();
        }

        if (!self.character().ruleSet().tracksHealth()) {
            Optional<Action> wearArmor = findAction(availableActions, WearArmor.class);
            if (wearArmor.isPresent()) {
                return wearArmor;
            }
            return defend;
        }

        return defend;
    }

    private double probabilityRoll() {
        return Math.max(0.0, Math.min(1.0, probabilityRoll.getAsDouble()));
    }

    private double opponentAttackLikelihood(Player opponent) {
        List<Action> opponentActions = opponent.availableActions();
        if (opponentActions.isEmpty()) {
            return 0.0;
        }

        long attackCount = opponentActions.stream()
            .filter(Attack.class::isInstance)
            .count();
        return (double) attackCount / opponentActions.size();
    }

    private <T extends Action> Optional<Action> findAction(List<Action> actions, Class<T> actionType) {
        return actions.stream()
            .filter(actionType::isInstance)
            .findFirst();
    }
}

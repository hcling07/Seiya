package seiya.controllers.rules;

import seiya.actions.Action;
import seiya.game.Player;

import java.util.List;
import java.util.Optional;

public interface AiDecisionRule {
    Optional<Action> chooseAction(Player self, Player opponent, List<Action> availableActions);
}

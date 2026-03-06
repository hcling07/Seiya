package seiya.controllers;

import seiya.actions.Action;
import seiya.game.Player;

import java.util.List;

public interface Controller {
    Action chooseAction(Player self, Player opponent, List<Action> availableActions);
}

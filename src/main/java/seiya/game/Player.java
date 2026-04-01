package seiya.game;

import seiya.actions.Action;
import seiya.actions.Defend;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
import seiya.characters.Character;
import seiya.controllers.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    private final String name;
    private final Character character;
    private final Controller controller;
    private final Gather gather;
    private final Defend defend;
    private final WearArmor wearArmor;
    private int turnsTaken;

    public Player(String name, Character character, Controller controller) {
        this.name = name;
        this.character = character;
        this.controller = controller;
        RuleSet ruleSet = character.ruleSet();
        this.gather = new Gather(ruleSet.gatherGain());
        this.defend = new Defend(ruleSet.defendReductionPercent(), ruleSet.defendValue());
        this.wearArmor = new WearArmor(ruleSet.wearArmorDefenseValue());
    }

    public String name() {
        return name;
    }

    public Character character() {
        return character;
    }

    public List<Action> availableActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(gather);
        actions.add(defend);
        actions.add(wearArmor);
        actions.addAll(character.attackMoves());
        actions.addAll(character.consumables());

        return actions.stream()
            .filter(action -> action.canExecute(character))
            .filter(this::isAvailableThisTurn)
            .collect(Collectors.toList());
    }

    public Action chooseAction(Player opponent) {
        List<Action> available = availableActions();
        Action action = controller.chooseAction(this, opponent, available);
        if (action == null || !available.contains(action)) {
            action = available.get(0);
        }
        return action;
    }

    public String executeAction(Action action, Player opponent) {
        return action.execute(character, opponent.character);
    }

    public String takeTurn(Player opponent) {
        Action action = chooseAction(opponent);
        return executeAction(action, opponent);
    }

    public void recordTurn() {
        turnsTaken++;
    }

    private boolean isAvailableThisTurn(Action action) {
        if (character.ruleSet() == RuleSet.CLASSIC && action instanceof WearArmor) {
            return turnsTaken > 0;
        }
        return true;
    }
}

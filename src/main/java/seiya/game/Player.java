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
    private final Gather gather = new Gather(2);
    private final Defend defend = new Defend(50);
    private final WearArmor wearArmor = new WearArmor();

    public Player(String name, Character character, Controller controller) {
        this.name = name;
        this.character = character;
        this.controller = controller;
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
            .collect(Collectors.toList());
    }

    public String takeTurn(Player opponent) {
        List<Action> available = availableActions();
        Action action = controller.chooseAction(this, opponent, available);
        if (action == null || !available.contains(action)) {
            action = available.get(0);
        }

        return action.execute(character, opponent.character);
    }
}

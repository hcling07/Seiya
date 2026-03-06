package seiya.controllers;

import seiya.actions.Action;
import seiya.game.Player;

import java.util.List;
import java.util.Scanner;

public class ConsoleHumanController implements Controller {
    private final Scanner scanner;

    public ConsoleHumanController(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Action chooseAction(Player self, Player opponent, List<Action> availableActions) {
        System.out.println(self.name() + "'s turn. Choose action:");
        for (int i = 0; i < availableActions.size(); i++) {
            System.out.println((i + 1) + ". " + availableActions.get(i).name());
        }
        System.out.print("> ");

        while (true) {
            String line = scanner.nextLine();
            try {
                int pick = Integer.parseInt(line.trim());
                if (pick >= 1 && pick <= availableActions.size()) {
                    return availableActions.get(pick - 1);
                }
            } catch (NumberFormatException ignored) {
                // Keep asking until we get a valid choice.
            }
            System.out.print("Invalid choice. Enter a number from 1 to " + availableActions.size() + ": ");
        }
    }
}

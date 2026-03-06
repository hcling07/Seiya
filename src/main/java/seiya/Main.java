package seiya;

import seiya.characters.Seiya;
import seiya.characters.Shiryu;
import seiya.controllers.BasicAiController;
import seiya.controllers.ConsoleHumanController;
import seiya.controllers.Controller;
import seiya.game.BattleGame;
import seiya.game.Player;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Controller ai = new BasicAiController();
        Controller human = new ConsoleHumanController(scanner);

        String mode = args.length == 0 ? "hva" : args[0].toLowerCase();
        Controller p1Controller = human;
        Controller p2Controller = ai;

        if ("hvh".equals(mode)) {
            p2Controller = human;
        } else if ("ava".equals(mode)) {
            p1Controller = ai;
            p2Controller = ai;
        }

        Player p1 = new Player("Player 1", new Seiya(), p1Controller);
        Player p2 = new Player("Player 2", new Shiryu(), p2Controller);
        BattleGame game = new BattleGame(p1, p2);
        game.run(System.out);
    }
}

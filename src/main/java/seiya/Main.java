package seiya;

import seiya.characters.Hyoga;
import seiya.characters.Seiya;
import seiya.controllers.BasicAiController;
import seiya.controllers.Controller;
import seiya.game.BattleGame;
import seiya.game.Player;
import seiya.game.RuleSet;
import seiya.ui.BattleUi;

public class Main {
    public static void main(String[] args) {
        String mode = args.length == 0 ? "ui" : args[0].toLowerCase();
        if ("ui".equals(mode)) {
            BattleUi.launch();
            return;
        }

        if ("ava".equals(mode)) {
            RuleSet ruleSet = args.length > 1 ? RuleSet.valueOf(args[1].toUpperCase()) : RuleSet.DEFAULT;
            Controller ai = new BasicAiController();
            Player p1 = new Player("Player 1", new Seiya(ruleSet), ai);
            Player p2 = new Player("Player 2", new Hyoga(ruleSet), ai);
            BattleGame game = new BattleGame(p1, p2);
            game.run(System.out);
            return;
        }

        throw new IllegalArgumentException("Unsupported mode: " + mode + ". Use ui or ava.");
    }
}

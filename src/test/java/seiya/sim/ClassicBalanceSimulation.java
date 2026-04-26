package seiya.sim;

import seiya.actions.Action;
import seiya.characters.Hyoga;
import seiya.characters.Seiya;
import seiya.characters.Shiryu;
import seiya.controllers.BasicAiController;
import seiya.game.Player;
import seiya.game.RuleSet;
import seiya.game.TurnResolver;

import java.util.function.Function;

public final class ClassicBalanceSimulation {
    private static final int DEFAULT_MATCHES = 20000;
    private static final int MAX_ROUNDS = 200;

    private ClassicBalanceSimulation() {
    }

    public static void main(String[] args) {
        int matches = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_MATCHES;
        CharacterFactory[] characters = new CharacterFactory[] {
            new CharacterFactory("Seiya", Seiya::new),
            new CharacterFactory("Shiryu", Shiryu::new),
            new CharacterFactory("Hyoga", Hyoga::new)
        };

        for (CharacterFactory first : characters) {
            for (CharacterFactory second : characters) {
                if (first == second) {
                    continue;
                }

                MatchSummary summary = runMatches(first, second, matches);
                System.out.printf(
                    "%s vs %s: %s %.1f%%, %s %.1f%%, draws %.1f%%%n",
                    first.name,
                    second.name,
                    first.name,
                    100.0 * summary.firstWins / matches,
                    second.name,
                    100.0 * summary.secondWins / matches,
                    100.0 * summary.draws / matches
                );
            }
        }
    }

    private static MatchSummary runMatches(CharacterFactory first, CharacterFactory second, int matches) {
        int firstWins = 0;
        int secondWins = 0;
        int draws = 0;

        for (int i = 0; i < matches; i++) {
            int result = runMatch(first, second);
            if (result > 0) {
                firstWins++;
            } else if (result < 0) {
                secondWins++;
            } else {
                draws++;
            }
        }

        return new MatchSummary(firstWins, secondWins, draws);
    }

    private static int runMatch(CharacterFactory first, CharacterFactory second) {
        Player playerOne = new Player("P1", first.create.apply(RuleSet.CLASSIC), new BasicAiController());
        Player playerTwo = new Player("P2", second.create.apply(RuleSet.CLASSIC), new BasicAiController());

        for (int round = 1;
             round <= MAX_ROUNDS && playerOne.character().isAlive() && playerTwo.character().isAlive();
             round++) {
            Action actionOne = playerOne.chooseAction(playerTwo);
            Action actionTwo = playerTwo.chooseAction(playerOne);
            TurnResolver.resolve(playerOne, actionOne, playerTwo, actionTwo);
        }

        if (playerOne.character().isAlive() && !playerTwo.character().isAlive()) {
            return 1;
        }
        if (!playerOne.character().isAlive() && playerTwo.character().isAlive()) {
            return -1;
        }

        int armorCompare = Integer.compare(playerOne.character().armorWorn(), playerTwo.character().armorWorn());
        if (armorCompare != 0) {
            return armorCompare;
        }
        return Double.compare(playerOne.character().spirit(), playerTwo.character().spirit());
    }

    private static final class CharacterFactory {
        private final String name;
        private final Function<RuleSet, seiya.characters.Character> create;

        private CharacterFactory(String name, Function<RuleSet, seiya.characters.Character> create) {
            this.name = name;
            this.create = create;
        }
    }

    private static final class MatchSummary {
        private final int firstWins;
        private final int secondWins;
        private final int draws;

        private MatchSummary(int firstWins, int secondWins, int draws) {
            this.firstWins = firstWins;
            this.secondWins = secondWins;
            this.draws = draws;
        }
    }
}

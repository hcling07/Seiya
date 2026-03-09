package seiya.game;

import seiya.actions.Action;
import seiya.util.NumberFormatter;

import java.io.PrintStream;
import java.util.List;

public class BattleGame {
    private final Player playerOne;
    private final Player playerTwo;
    private final int maxRounds;

    public BattleGame(Player playerOne, Player playerTwo) {
        this(playerOne, playerTwo, 200);
    }

    public BattleGame(Player playerOne, Player playerTwo, int maxRounds) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.maxRounds = maxRounds;
    }

    public String run() {
        return run(null);
    }

    public String run(PrintStream out) {
        StringBuilder log = new StringBuilder();
        int round = 1;

        while (playerOne.character().isAlive() && playerTwo.character().isAlive() && round <= maxRounds) {
            appendLine(log, "Round " + round, out);
            appendLine(log, statusLine(), out);

            Action actionOne = playerOne.chooseAction(playerTwo);
            Action actionTwo = playerTwo.chooseAction(playerOne);
            List<String> turnLogs = TurnResolver.resolve(playerOne, actionOne, playerTwo, actionTwo);
            for (String turnLog : turnLogs) {
                appendLine(log, turnLog, out);
            }
            appendLine(log, "", out);
            round++;
        }

        Player winner = resolveWinner();
        if (winner == null) {
            appendLine(log, "Result: Draw", out);
        } else {
            appendLine(log, "Winner: " + winner.name() + " (" + winner.character().name() + ")", out);
        }
        return log.toString();
    }

    private String statusLine() {
        return playerOne.name() + " HP=" + NumberFormatter.fmt(playerOne.character().health())
            + ", Spirit=" + NumberFormatter.fmt(playerOne.character().spirit())
            + ", Armor=" + playerOne.character().armorWorn()
            + " | "
            + playerTwo.name() + " HP=" + NumberFormatter.fmt(playerTwo.character().health())
            + ", Spirit=" + NumberFormatter.fmt(playerTwo.character().spirit())
            + ", Armor=" + playerTwo.character().armorWorn();
    }

    private Player resolveWinner() {
        if (playerOne.character().isAlive() && !playerTwo.character().isAlive()) {
            return playerOne;
        }
        if (!playerOne.character().isAlive() && playerTwo.character().isAlive()) {
            return playerTwo;
        }

        double hpOne = playerOne.character().health();
        double hpTwo = playerTwo.character().health();
        if (hpOne > hpTwo) {
            return playerOne;
        }
        if (hpTwo > hpOne) {
            return playerTwo;
        }

        double spiritOne = playerOne.character().spirit();
        double spiritTwo = playerTwo.character().spirit();
        if (spiritOne > spiritTwo) {
            return playerOne;
        }
        if (spiritTwo > spiritOne) {
            return playerTwo;
        }

        return null;
    }

    private void appendLine(StringBuilder log, String line, PrintStream out) {
        log.append(line).append('\n');
        if (out != null) {
            out.println(line);
        }
    }
}

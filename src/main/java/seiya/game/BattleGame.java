package seiya.game;

import java.io.PrintStream;

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
            executeTurn(playerOne, playerTwo, log, out);
            if (!playerTwo.character().isAlive()) {
                break;
            }
            executeTurn(playerTwo, playerOne, log, out);
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

    private void executeTurn(Player actor, Player target, StringBuilder log, PrintStream out) {
        String turnResult = actor.takeTurn(target);
        appendLine(log, actor.name() + ": " + turnResult, out);
        appendLine(log, statusLine(), out);
    }

    private String statusLine() {
        return playerOne.name() + " HP=" + playerOne.character().health()
            + ", Spirit=" + playerOne.character().spirit()
            + ", Armor=" + playerOne.character().armorWorn()
            + " | "
            + playerTwo.name() + " HP=" + playerTwo.character().health()
            + ", Spirit=" + playerTwo.character().spirit()
            + ", Armor=" + playerTwo.character().armorWorn();
    }

    private Player resolveWinner() {
        if (playerOne.character().isAlive() && !playerTwo.character().isAlive()) {
            return playerOne;
        }
        if (!playerOne.character().isAlive() && playerTwo.character().isAlive()) {
            return playerTwo;
        }

        int hpOne = playerOne.character().health();
        int hpTwo = playerTwo.character().health();
        if (hpOne > hpTwo) {
            return playerOne;
        }
        if (hpTwo > hpOne) {
            return playerTwo;
        }

        int spiritOne = playerOne.character().spirit();
        int spiritTwo = playerTwo.character().spirit();
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

package seiya.ui;

import seiya.actions.Action;
import seiya.characters.Seiya;
import seiya.characters.Shiryu;
import seiya.controllers.BasicAiController;
import seiya.controllers.Controller;
import seiya.game.Player;
import seiya.game.TurnResolver;
import seiya.util.NumberFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class BattleUi {
    private final JFrame frame;
    private final JTextArea statusArea;
    private final JTextArea logArea;
    private final JPanel actionPanel;

    private final Controller aiController = new BasicAiController();
    private final Player humanPlayer = new Player("Player 1", new Seiya(), (self, opponent, available) -> available.get(0));
    private final Player aiPlayer = new Player("Player 2", new Shiryu(), aiController);
    private boolean battleEnded;

    private BattleUi() {
        frame = new JFrame("Seiya Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout(8, 8));

        statusArea = new JTextArea(8, 40);
        statusArea.setEditable(false);
        logArea = new JTextArea(16, 40);
        logArea.setEditable(false);

        actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        topPanel.add(new JLabel("Character Status"), BorderLayout.NORTH);
        topPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
        centerPanel.add(new JLabel("Action Log"), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(4, 4));
        bottomPanel.add(new JLabel("Available Actions"), BorderLayout.NORTH);
        bottomPanel.add(actionPanel, BorderLayout.CENTER);

        JPanel content = new JPanel(new GridLayout(3, 1, 8, 8));
        content.add(topPanel);
        content.add(centerPanel);
        content.add(bottomPanel);

        frame.add(content, BorderLayout.CENTER);

        appendLog("Battle started. Choose an action.");
        refreshStatus();
        refreshActionButtons();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            BattleUi ui = new BattleUi();
            ui.frame.setVisible(true);
        });
    }

    private void refreshStatus() {
        statusArea.setText(buildPlayerStatus(humanPlayer) + "\n\n" + buildPlayerStatus(aiPlayer));
    }

    private String buildPlayerStatus(Player player) {
        return player.name() + " (" + player.character().name() + ")\n"
            + "HP: " + NumberFormatter.fmt(player.character().health()) + "/"
            + NumberFormatter.fmt(player.character().maxHealth()) + "\n"
            + "Spirit: " + NumberFormatter.fmt(player.character().spirit()) + "\n"
            + "Armor: " + player.character().armorWorn() + "/" + player.character().totalArmor() + "\n"
            + "Consumables left: " + player.character().consumables().size();
    }

    private void refreshActionButtons() {
        actionPanel.removeAll();
        List<Action> actions = humanPlayer.availableActions();
        for (Action action : actions) {
            JButton button = new JButton(action.name());
            button.addActionListener(e -> onHumanAction(action));
            actionPanel.add(button);
        }
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private void onHumanAction(Action action) {
        if (battleEnded) {
            return;
        }

        Action aiAction = aiPlayer.chooseAction(humanPlayer);
        for (String line : TurnResolver.resolve(humanPlayer, action, aiPlayer, aiAction)) {
            appendLog(line);
        }
        refreshStatus();

        if (checkBattleEnd()) {
            return;
        }

        refreshActionButtons();
    }

    private boolean checkBattleEnd() {
        if (!humanPlayer.character().isAlive() || !aiPlayer.character().isAlive()) {
            battleEnded = true;
            String winner = humanPlayer.character().isAlive() ? humanPlayer.name() : aiPlayer.name();
            appendLog("Winner: " + winner);
            actionPanel.removeAll();
            actionPanel.revalidate();
            actionPanel.repaint();
            return true;
        }
        return false;
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}

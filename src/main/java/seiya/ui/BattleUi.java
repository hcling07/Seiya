package seiya.ui;

import seiya.actions.Action;
import seiya.characters.Character;
import seiya.characters.Hyoga;
import seiya.characters.Seiya;
import seiya.characters.Shiryu;
import seiya.controllers.BasicAiController;
import seiya.controllers.Controller;
import seiya.game.Player;
import seiya.game.TurnResolver;
import seiya.util.NumberFormatter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Supplier;

public class BattleUi {
    private static final String START_SCREEN = "start";
    private static final String BATTLE_SCREEN = "battle";

    private final JFrame frame;
    private final JPanel rootPanel;
    private final CardLayout cardLayout;
    private final JComboBox<CharacterOption> humanSelector;
    private final JComboBox<CharacterOption> aiSelector;
    private final JTextArea statusArea;
    private final JTextArea logArea;
    private final JPanel actionPanel;

    private final Controller aiController = new BasicAiController();
    private Player humanPlayer;
    private Player aiPlayer;
    private boolean battleEnded;

    private BattleUi() {
        frame = new JFrame("Seiya Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        humanSelector = new JComboBox<>(characterOptions());
        aiSelector = new JComboBox<>(characterOptions());
        humanSelector.setSelectedIndex(0);
        aiSelector.setSelectedIndex(1);

        statusArea = new JTextArea(8, 40);
        statusArea.setEditable(false);
        logArea = new JTextArea(16, 40);
        logArea.setEditable(false);
        actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        rootPanel.add(buildStartPanel(), START_SCREEN);
        rootPanel.add(buildBattlePanel(), BATTLE_SCREEN);
        frame.setContentPane(rootPanel);
        cardLayout.show(rootPanel, START_SCREEN);
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            BattleUi ui = new BattleUi();
            ui.frame.setVisible(true);
        });
    }

    private JPanel buildStartPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Seiya Battle Setup");
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JPanel humanPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        humanPanel.add(new JLabel("Human Character"));
        humanPanel.add(humanSelector);

        JPanel aiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        aiPanel.add(new JLabel("AI Character"));
        aiPanel.add(aiSelector);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startBattle());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitAllJavaProcesses());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.add(startButton);
        buttonPanel.add(exitButton);

        panel.add(new JLabel(" "));
        panel.add(title);
        panel.add(new JLabel(" "));
        panel.add(humanPanel);
        panel.add(aiPanel);
        panel.add(buttonPanel);
        return panel;
    }

    private JPanel buildBattlePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

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

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void startBattle() {
        Character humanCharacter = selectedOption(humanSelector).factory.get();
        Character aiCharacter = selectedOption(aiSelector).factory.get();
        humanPlayer = new Player("Player 1", humanCharacter, (self, opponent, available) -> available.get(0));
        aiPlayer = new Player("Player 2", aiCharacter, aiController);
        battleEnded = false;

        statusArea.setText("");
        logArea.setText("");
        appendLog("Battle started. Choose an action.");
        refreshStatus();
        refreshActionButtons();
        cardLayout.show(rootPanel, BATTLE_SCREEN);
    }

    private CharacterOption selectedOption(JComboBox<CharacterOption> selector) {
        CharacterOption option = (CharacterOption) selector.getSelectedItem();
        if (option == null) {
            return CharacterOption.SEIYA;
        }
        return option;
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

    private void exitAllJavaProcesses() {
        try {
            Runtime.getRuntime().exec(new String[] {"pkill", "-f", "java"});
        } catch (Exception ignored) {
            // Fall through to local exit if process termination command fails.
        }
        frame.dispose();
        System.exit(0);
    }

    private CharacterOption[] characterOptions() {
        return CharacterOption.values();
    }

    private enum CharacterOption {
        SEIYA("Seiya", Seiya::new),
        SHIRYU("Shiryu", Shiryu::new),
        HYOGA("Hyoga", Hyoga::new);

        private final String label;
        private final Supplier<Character> factory;

        CharacterOption(String label, Supplier<Character> factory) {
            this.label = label;
            this.factory = factory;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}

package seiya.ui;

import seiya.actions.Action;
import seiya.characters.Character;
import seiya.characters.Hyoga;
import seiya.characters.Seiya;
import seiya.characters.Shiryu;
import seiya.controllers.BasicAiController;
import seiya.controllers.Controller;
import seiya.game.Player;
import seiya.game.RuleSet;
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

public class BattleUi {
    private static final String START_SCREEN = "start";
    private static final String BATTLE_SCREEN = "battle";

    private final JFrame frame;
    private final JPanel rootPanel;
    private final CardLayout cardLayout;
    private final JComboBox<CharacterOption> humanSelector;
    private final JComboBox<CharacterOption> aiSelector;
    private final JComboBox<RuleSet> ruleSetSelector;
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
        ruleSetSelector = new JComboBox<>(RuleSet.values());
        humanSelector.setSelectedIndex(0);
        aiSelector.setSelectedIndex(1);
        ruleSetSelector.setSelectedItem(RuleSet.DEFAULT);

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

        JPanel rulePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        rulePanel.add(new JLabel("Rule Set"));
        rulePanel.add(ruleSetSelector);

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
        panel.add(rulePanel);
        panel.add(buttonPanel);
        return panel;
    }

    private JPanel buildBattlePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(e -> startBattle());
        JButton mainMenuButton = new JButton("Main Menu");
        mainMenuButton.addActionListener(e -> returnToMainMenu());
        controlPanel.add(playAgainButton);
        controlPanel.add(mainMenuButton);

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

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void startBattle() {
        RuleSet ruleSet = selectedRuleSet();
        Character humanCharacter = selectedOption(humanSelector).create(ruleSet);
        Character aiCharacter = selectedOption(aiSelector).create(ruleSet);
        humanPlayer = new Player("Player 1", humanCharacter, (self, opponent, available) -> available.get(0));
        aiPlayer = new Player("Player 2", aiCharacter, aiController);
        battleEnded = false;

        statusArea.setText("");
        logArea.setText("");
        appendLog("Battle started with " + ruleSet + " rules. Choose an action.");
        refreshStatus();
        refreshActionButtons();
        cardLayout.show(rootPanel, BATTLE_SCREEN);
    }

    private RuleSet selectedRuleSet() {
        RuleSet ruleSet = (RuleSet) ruleSetSelector.getSelectedItem();
        if (ruleSet == null) {
            return RuleSet.DEFAULT;
        }
        return ruleSet;
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
        if (!player.character().ruleSet().tracksHealth()) {
            return player.name() + " (" + player.character().name() + ")\n"
                + "Spirit: " + NumberFormatter.fmt(player.character().spirit()) + "\n"
                + "Armor Worn: " + player.character().armorWorn() + "\n"
                + "Remaining Armors: " + player.character().remainingArmor() + "\n"
                + "Consumables left: " + player.character().consumables().size();
        }

        return player.name() + " (" + player.character().name() + ")\n"
            + "HP: " + NumberFormatter.fmt(player.character().health()) + "/"
            + NumberFormatter.fmt(player.character().maxHealth()) + "\n"
            + "Spirit: " + NumberFormatter.fmt(player.character().spirit()) + "\n"
            + "Armor Worn: " + player.character().armorWorn() + "\n"
            + "Remaining Armors: " + player.character().remainingArmor() + "\n"
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

    private void returnToMainMenu() {
        battleEnded = false;
        statusArea.setText("");
        logArea.setText("");
        actionPanel.removeAll();
        actionPanel.revalidate();
        actionPanel.repaint();
        cardLayout.show(rootPanel, START_SCREEN);
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
        SEIYA("Seiya") {
            @Override
            Character create(RuleSet ruleSet) {
                return new Seiya(ruleSet);
            }
        },
        SHIRYU("Shiryu") {
            @Override
            Character create(RuleSet ruleSet) {
                return new Shiryu(ruleSet);
            }
        },
        HYOGA("Hyoga") {
            @Override
            Character create(RuleSet ruleSet) {
                return new Hyoga(ruleSet);
            }
        };

        private final String label;

        CharacterOption(String label) {
            this.label = label;
        }

        abstract Character create(RuleSet ruleSet);

        @Override
        public String toString() {
            return label;
        }
    }
}

package seiya.ui;

import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.actions.Defend;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class BattleUi {
    private static final String START_SCREEN = "start";
    private static final String BATTLE_SCREEN = "battle";
    private static final Color WINNER_BACKGROUND = new Color(220, 245, 220);
    private static final Dimension ACTION_SCROLL_SIZE = new Dimension(480, 120);

    private final JFrame frame;
    private final JPanel rootPanel;
    private final CardLayout cardLayout;
    private final JComboBox<CharacterOption> humanSelector;
    private final JComboBox<CharacterOption> aiSelector;
    private final JComboBox<OpponentMode> opponentModeSelector;
    private final JComboBox<RuleSet> ruleSetSelector;
    private final JTextArea playerOneStatusArea;
    private final JTextArea playerTwoStatusArea;
    private final Color originalStatusBackground;
    private final JTextArea logArea;
    private final JPanel playerOneActionPanel;
    private final JPanel playerTwoActionPanel;
    private final JPanel playerOneColumnPanel;
    private final JPanel playerTwoColumnPanel;
    private final JPanel playerOneStatusPanel;
    private final JPanel playerTwoStatusPanel;
    private final JLabel playerOneLockMessageLabel;
    private final JLabel playerTwoLockMessageLabel;

    private final Controller aiController = new BasicAiController();
    private Player humanPlayer;
    private Player aiPlayer;
    private boolean humanVsHuman;
    private Action pendingPlayerOneAction;
    private Action pendingPlayerTwoAction;
    private boolean battleEnded;

    private BattleUi() {
        frame = new JFrame("Seiya Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 720);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        humanSelector = new JComboBox<>(characterOptions());
        aiSelector = new JComboBox<>(characterOptions());
        opponentModeSelector = new JComboBox<>(OpponentMode.values());
        ruleSetSelector = new JComboBox<>(RuleSet.values());
        humanSelector.setSelectedIndex(0);
        aiSelector.setSelectedIndex(1);
        opponentModeSelector.setSelectedItem(OpponentMode.AI);
        ruleSetSelector.setSelectedItem(RuleSet.DEFAULT);

        playerOneStatusArea = new JTextArea(6, 24);
        playerOneStatusArea.setEditable(false);
        playerTwoStatusArea = new JTextArea(6, 24);
        playerTwoStatusArea.setEditable(false);
        originalStatusBackground = playerOneStatusArea.getBackground();
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        playerOneActionPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        playerTwoActionPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        playerOneLockMessageLabel = new JLabel(" ");
        playerTwoLockMessageLabel = new JLabel(" ");
        playerOneStatusPanel = buildStatusPanel("Player 1", playerOneStatusArea);
        playerTwoStatusPanel = buildStatusPanel("Player 2", playerTwoStatusArea);
        playerOneColumnPanel = buildPlayerColumn("Player 1", playerOneStatusPanel, playerOneActionPanel, playerOneLockMessageLabel);
        playerTwoColumnPanel = buildPlayerColumn("Player 2", playerTwoStatusPanel, playerTwoActionPanel, playerTwoLockMessageLabel);

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
        humanPanel.add(new JLabel("Player 1 Character"));
        humanPanel.add(humanSelector);

        JPanel opponentModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        opponentModePanel.add(new JLabel("Opponent Type"));
        opponentModePanel.add(opponentModeSelector);

        JPanel aiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        aiPanel.add(new JLabel("Player 2 Character"));
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
        panel.add(opponentModePanel);
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

        JPanel playerColumns = new JPanel(new GridLayout(1, 2, 8, 8));
        playerColumns.add(playerOneColumnPanel);
        playerColumns.add(playerTwoColumnPanel);
        JScrollPane playerColumnsScroll = new JScrollPane(playerColumns);

        JPanel logPanel = new JPanel(new BorderLayout(4, 4));
        logPanel.add(new JLabel("Action Log"), BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(playerColumnsScroll, BorderLayout.CENTER);
        panel.add(logPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStatusPanel(String title, JTextArea statusArea) {
        JPanel statusPanel = new JPanel(new BorderLayout(4, 4));
        statusPanel.add(new JLabel(title + " Status"), BorderLayout.NORTH);
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        return statusPanel;
    }

    private JPanel buildPlayerColumn(String title, JPanel statusPanel, JPanel actionPanel, JLabel lockMessageLabel) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        JPanel actionsPanel = new JPanel(new BorderLayout(4, 4));
        actionsPanel.add(new JLabel(title + " Actions"), BorderLayout.NORTH);
        JScrollPane actionScrollPane = new JScrollPane(actionPanel);
        actionScrollPane.setPreferredSize(ACTION_SCROLL_SIZE);
        actionsPanel.add(actionScrollPane, BorderLayout.CENTER);
        actionsPanel.add(lockMessageLabel, BorderLayout.SOUTH);

        panel.add(actionsPanel, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.CENTER);
        return panel;
    }

    private void startBattle() {
        RuleSet ruleSet = selectedRuleSet();
        Character humanCharacter = selectedOption(humanSelector).create(ruleSet);
        Character opponentCharacter = selectedOption(aiSelector).create(ruleSet);
        humanVsHuman = selectedOpponentMode() == OpponentMode.HUMAN;
        humanPlayer = new Player("Player 1", humanCharacter, (self, opponent, available) -> available.get(0));
        aiPlayer = new Player(
            "Player 2",
            opponentCharacter,
            humanVsHuman ? (self, opponent, available) -> available.get(0) : aiController
        );
        pendingPlayerOneAction = null;
        pendingPlayerTwoAction = null;
        battleEnded = false;

        playerOneStatusArea.setText("");
        playerTwoStatusArea.setText("");
        playerOneLockMessageLabel.setText(" ");
        playerTwoLockMessageLabel.setText(" ");
        clearWinnerHighlight();
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

    private OpponentMode selectedOpponentMode() {
        OpponentMode mode = (OpponentMode) opponentModeSelector.getSelectedItem();
        if (mode == null) {
            return OpponentMode.AI;
        }
        return mode;
    }

    private CharacterOption selectedOption(JComboBox<CharacterOption> selector) {
        CharacterOption option = (CharacterOption) selector.getSelectedItem();
        if (option == null) {
            return CharacterOption.SEIYA;
        }
        return option;
    }

    private void refreshStatus() {
        playerOneStatusArea.setText(buildPlayerStatus(humanPlayer));
        playerTwoStatusArea.setText(buildPlayerStatus(aiPlayer));
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
        refreshPlayerActionButtons(
            humanPlayer,
            playerOneActionPanel,
            pendingPlayerOneAction,
            playerOneLockMessageLabel,
            this::onPlayerOneAction
        );
        if (humanVsHuman) {
            refreshPlayerActionButtons(
                aiPlayer,
                playerTwoActionPanel,
                pendingPlayerTwoAction,
                playerTwoLockMessageLabel,
                this::onPlayerTwoAction
            );
        } else {
            playerTwoActionPanel.removeAll();
            playerTwoLockMessageLabel.setText(" ");
            playerTwoActionPanel.add(new JLabel("AI chooses automatically."));
            playerTwoActionPanel.revalidate();
            playerTwoActionPanel.repaint();
        }
    }

    private void refreshPlayerActionButtons(
        Player player,
        JPanel actionPanel,
        Action pendingAction,
        JLabel lockMessageLabel,
        ActionHandler actionHandler
    ) {
        actionPanel.removeAll();
        lockMessageLabel.setText(pendingAction == null ? " " : "Waiting for other player.");
        if (battleEnded) {
            lockMessageLabel.setText(" ");
            addCategoryColumn("General", player.availableActions(), actionPanel, actionHandler, ActionCategory.GENERAL, false);
            addCategoryColumn("Attack", player.availableActions(), actionPanel, actionHandler, ActionCategory.ATTACK, false);
            addCategoryColumn("Consumable", player.availableActions(), actionPanel, actionHandler, ActionCategory.CONSUMABLE, false);
            actionPanel.revalidate();
            actionPanel.repaint();
            return;
        }

        boolean enabled = pendingAction == null;
        addCategoryColumn("General", player.availableActions(), actionPanel, actionHandler, ActionCategory.GENERAL, enabled);
        addCategoryColumn("Attack", player.availableActions(), actionPanel, actionHandler, ActionCategory.ATTACK, enabled);
        addCategoryColumn("Consumable", player.availableActions(), actionPanel, actionHandler, ActionCategory.CONSUMABLE, enabled);

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    private void addCategoryColumn(
        String label,
        List<Action> actions,
        JPanel actionPanel,
        ActionHandler actionHandler,
        ActionCategory category,
        boolean enabled
    ) {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.add(new JLabel(label));

        for (Action action : actions) {
            if (categoryFor(action) != category) {
                continue;
            }
            JButton button = new JButton(action.name());
            button.setEnabled(enabled);
            button.setAlignmentX(JButton.LEFT_ALIGNMENT);
            button.addActionListener(e -> actionHandler.handle(action));
            categoryPanel.add(button);
        }

        categoryPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        actionPanel.add(categoryPanel);
    }

    private ActionCategory categoryFor(Action action) {
        if (action instanceof Gather || action instanceof WearArmor || action instanceof Defend) {
            return ActionCategory.GENERAL;
        }
        if (action instanceof ConsumableAttack) {
            return ActionCategory.CONSUMABLE;
        }
        if (action instanceof Attack || action.attackValue() > 0.0) {
            return ActionCategory.ATTACK;
        }
        return ActionCategory.GENERAL;
    }

    private void onPlayerOneAction(Action action) {
        if (battleEnded) {
            return;
        }

        if (!humanVsHuman) {
            resolveTurn(action, aiPlayer.chooseAction(humanPlayer));
            return;
        }

        pendingPlayerOneAction = action;
        appendLog(humanPlayer.name() + " locked in an action.");
        refreshActionButtons();
        resolvePendingHumanTurn();
    }

    private void onPlayerTwoAction(Action action) {
        if (battleEnded || !humanVsHuman) {
            return;
        }

        pendingPlayerTwoAction = action;
        appendLog(aiPlayer.name() + " locked in an action.");
        refreshActionButtons();
        resolvePendingHumanTurn();
    }

    private void resolvePendingHumanTurn() {
        if (pendingPlayerOneAction == null || pendingPlayerTwoAction == null) {
            return;
        }

        Action actionOne = pendingPlayerOneAction;
        Action actionTwo = pendingPlayerTwoAction;
        pendingPlayerOneAction = null;
        pendingPlayerTwoAction = null;
        resolveTurn(actionOne, actionTwo);
    }

    private void resolveTurn(Action actionOne, Action actionTwo) {
        for (String line : TurnResolver.resolve(humanPlayer, actionOne, aiPlayer, actionTwo)) {
            appendLog(line);
        }
        refreshStatus();

        if (!checkBattleEnd()) {
            refreshActionButtons();
        }
    }

    private boolean checkBattleEnd() {
        if (!humanPlayer.character().isAlive() || !aiPlayer.character().isAlive()) {
            battleEnded = true;
            if (humanPlayer.character().isAlive() == aiPlayer.character().isAlive()) {
                appendLog("Result: Draw");
            } else {
                String winner = humanPlayer.character().isAlive() ? humanPlayer.name() : aiPlayer.name();
                appendLog("Winner: " + winner);
                highlightWinner(humanPlayer.character().isAlive() ? playerOneStatusPanel : playerTwoStatusPanel);
            }
            refreshActionButtons();
            return true;
        }
        return false;
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void highlightWinner(JPanel winnerPanel) {
        clearWinnerHighlight();
        setPanelBackground(winnerPanel, WINNER_BACKGROUND);
        winnerPanel.repaint();
    }

    private void clearWinnerHighlight() {
        setPanelBackground(playerOneStatusPanel, null);
        setPanelBackground(playerTwoStatusPanel, null);
        playerOneStatusPanel.repaint();
        playerTwoStatusPanel.repaint();
    }

    private void setPanelBackground(Component component, Color color) {
        if (component instanceof JTextArea) {
            component.setBackground(color == null ? originalStatusBackground : color);
            return;
        }
        if (component instanceof JPanel || component instanceof JLabel) {
            component.setBackground(color);
            if (component instanceof JPanel) {
                ((JPanel) component).setOpaque(color != null);
            } else {
                ((JLabel) component).setOpaque(color != null);
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setPanelBackground(child, color);
            }
        }
    }

    private void returnToMainMenu() {
        battleEnded = false;
        pendingPlayerOneAction = null;
        pendingPlayerTwoAction = null;
        playerOneStatusArea.setText("");
        playerTwoStatusArea.setText("");
        playerOneLockMessageLabel.setText(" ");
        playerTwoLockMessageLabel.setText(" ");
        clearWinnerHighlight();
        logArea.setText("");
        playerOneActionPanel.removeAll();
        playerOneActionPanel.revalidate();
        playerOneActionPanel.repaint();
        playerTwoActionPanel.removeAll();
        playerTwoActionPanel.revalidate();
        playerTwoActionPanel.repaint();
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

    private interface ActionHandler {
        void handle(Action action);
    }

    private enum ActionCategory {
        GENERAL,
        ATTACK,
        CONSUMABLE
    }

    private enum OpponentMode {
        AI("AI Opponent"),
        HUMAN("Human Opponent");

        private final String label;

        OpponentMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
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

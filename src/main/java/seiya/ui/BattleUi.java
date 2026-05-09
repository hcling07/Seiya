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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;

public class BattleUi {
    private static final String START_SCREEN = "start";
    private static final String BATTLE_SCREEN = "battle";
    private static final Color WINNER_BACKGROUND = new Color(220, 245, 220);
    private static final Color SPRITE_BACKGROUND = new Color(245, 245, 245);
    private static final Color SPRITE_BORDER = new Color(180, 180, 180);
    private static final Dimension ACTION_SCROLL_SIZE = new Dimension(480, 120);
    private static final Dimension SPRITE_SIZE = new Dimension(156, 156);
    private static final int SPRITE_IMAGE_SIZE = 138;
    private static final double GIF_PLAYBACK_SPEED = 2.0;
    private static final int MIN_GIF_FRAME_DELAY_MS = 20;
    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    private final JFrame frame;
    private final JPanel rootPanel;
    private final CardLayout cardLayout;
    private final JComboBox<CharacterOption> humanSelector;
    private final JComboBox<CharacterOption> aiSelector;
    private final JComboBox<OpponentMode> opponentModeSelector;
    private final JComboBox<RuleSet> ruleSetSelector;
    private final JLabel humanPreviewLabel;
    private final JLabel aiPreviewLabel;
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
    private final JLabel playerOneSpriteLabel;
    private final JLabel playerTwoSpriteLabel;
    private final JLabel playerOneLockMessageLabel;
    private final JLabel playerTwoLockMessageLabel;

    private final Controller aiController = new BasicAiController();
    private Player humanPlayer;
    private Player aiPlayer;
    private boolean humanVsHuman;
    private Action pendingPlayerOneAction;
    private Action pendingPlayerTwoAction;
    private boolean battleEnded;
    private Timer playerOneFightTimer;
    private Timer playerTwoFightTimer;
    private Timer playerOneDeathTimer;
    private Timer playerTwoDeathTimer;
    private Timer playerOneCelebrationTimer;
    private Timer playerTwoCelebrationTimer;

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
        humanPreviewLabel = buildSpriteLabel();
        aiPreviewLabel = buildSpriteLabel();
        humanSelector.addActionListener(e -> refreshStartPreviews());
        aiSelector.addActionListener(e -> refreshStartPreviews());

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
        playerOneSpriteLabel = buildSpriteLabel();
        playerTwoSpriteLabel = buildSpriteLabel();
        playerOneStatusPanel = buildStatusPanel("Player 1", playerOneStatusArea);
        playerTwoStatusPanel = buildStatusPanel("Player 2", playerTwoStatusArea);
        playerOneColumnPanel = buildPlayerColumn("Player 1", playerOneSpriteLabel, playerOneStatusPanel, playerOneActionPanel, playerOneLockMessageLabel);
        playerTwoColumnPanel = buildPlayerColumn("Player 2", playerTwoSpriteLabel, playerTwoStatusPanel, playerTwoActionPanel, playerTwoLockMessageLabel);

        rootPanel.add(buildStartPanel(), START_SCREEN);
        rootPanel.add(buildBattlePanel(), BATTLE_SCREEN);
        frame.setContentPane(rootPanel);
        refreshStartPreviews();
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
        humanPanel.add(humanPreviewLabel);

        JPanel opponentModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        opponentModePanel.add(new JLabel("Opponent Type"));
        opponentModePanel.add(opponentModeSelector);

        JPanel aiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        aiPanel.add(new JLabel("Player 2 Character"));
        aiPanel.add(aiSelector);
        aiPanel.add(aiPreviewLabel);

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

    private JPanel buildPlayerColumn(String title, JLabel spriteLabel, JPanel statusPanel, JPanel actionPanel, JLabel lockMessageLabel) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        JPanel actionsPanel = new JPanel(new BorderLayout(4, 4));
        actionsPanel.add(new JLabel(title + " Actions"), BorderLayout.NORTH);
        JScrollPane actionScrollPane = new JScrollPane(actionPanel);
        actionScrollPane.setPreferredSize(ACTION_SCROLL_SIZE);
        actionsPanel.add(actionScrollPane, BorderLayout.CENTER);
        actionsPanel.add(lockMessageLabel, BorderLayout.SOUTH);

        panel.add(actionsPanel, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.CENTER);
        panel.add(spriteLabel, BorderLayout.SOUTH);
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
        stopSpriteAnimations();

        playerOneStatusArea.setText("");
        playerTwoStatusArea.setText("");
        playerOneLockMessageLabel.setText(" ");
        playerTwoLockMessageLabel.setText(" ");
        clearWinnerHighlight();
        logArea.setText("");
        appendLog("Battle started with " + ruleSet + " rules. Choose an action.");
        refreshStatus();
        refreshBattleSprites();
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

    private void refreshStartPreviews() {
        setSpriteIcon(humanPreviewLabel, selectedOption(humanSelector).standingIcon(), selectedOption(humanSelector).toString());
        setSpriteIcon(aiPreviewLabel, selectedOption(aiSelector).standingIcon(), selectedOption(aiSelector).toString());
    }

    private void refreshBattleSprites() {
        playFightingAnimation(playerOneSpriteLabel, selectedOption(humanSelector), SpriteOrientation.SE, true);
        playFightingAnimation(playerTwoSpriteLabel, selectedOption(aiSelector), SpriteOrientation.SW, false);
    }

    private JLabel buildSpriteLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setPreferredSize(SPRITE_SIZE);
        label.setMinimumSize(SPRITE_SIZE);
        label.setOpaque(true);
        label.setBackground(SPRITE_BACKGROUND);
        label.setBorder(BorderFactory.createLineBorder(SPRITE_BORDER));
        return label;
    }

    private void setSpriteIcon(JLabel label, ImageIcon icon, String fallbackText) {
        label.setIcon(icon);
        label.setText(icon == null ? fallbackText : "");
    }

    private void playFightingAnimation(JLabel label, CharacterOption characterOption, SpriteOrientation orientation, boolean playerOne) {
        stopFightAnimation(playerOne);

        GifAnimation animation = characterOption.fightingAnimation(orientation);
        if (animation.frames.isEmpty()) {
            setSpriteIcon(label, null, characterOption.toString());
            return;
        }

        label.setIcon(animation.frames.get(0));
        label.setText("");
        Timer timer = new Timer(animation.delayAt(0), null);
        final int[] frameIndex = new int[] {0};
        timer.addActionListener(e -> {
            frameIndex[0] = (frameIndex[0] + 1) % animation.frames.size();
            label.setIcon(animation.frames.get(frameIndex[0]));
            timer.setDelay(animation.delayAt(frameIndex[0]));
        });
        if (playerOne) {
            playerOneFightTimer = timer;
        } else {
            playerTwoFightTimer = timer;
        }
        timer.start();
    }

    private void playDyingAnimation(JLabel label, CharacterOption characterOption, SpriteOrientation orientation, boolean playerOne) {
        playFiniteAnimation(
            label,
            characterOption,
            characterOption.dyingAnimation(orientation),
            playerOne,
            SpriteAnimationType.DYING
        );
    }

    private void playCelebrationAnimation(JLabel label, CharacterOption characterOption, SpriteOrientation orientation, boolean playerOne) {
        playFiniteAnimation(
            label,
            characterOption,
            characterOption.celebrationAnimation(orientation),
            playerOne,
            SpriteAnimationType.CELEBRATION
        );
    }

    private void playFiniteAnimation(
        JLabel label,
        CharacterOption characterOption,
        GifAnimation animation,
        boolean playerOne,
        SpriteAnimationType animationType
    ) {
        stopFightAnimation(playerOne);
        stopFiniteAnimation(playerOne, animationType);

        if (animation.frames.isEmpty()) {
            setSpriteIcon(label, null, characterOption.toString());
            return;
        }

        label.setIcon(animation.frames.get(0));
        label.setText("");
        Timer timer = new Timer(animation.delayAt(0), null);
        final int[] frameIndex = new int[] {0};
        timer.addActionListener(e -> {
            frameIndex[0]++;
            if (frameIndex[0] >= animation.frames.size()) {
                timer.stop();
                label.setIcon(animation.frames.get(animation.frames.size() - 1));
                return;
            }
            label.setIcon(animation.frames.get(frameIndex[0]));
            timer.setDelay(animation.delayAt(frameIndex[0]));
        });
        setFiniteAnimationTimer(playerOne, animationType, timer);
        timer.start();
    }

    private void stopFiniteAnimation(boolean playerOne, SpriteAnimationType animationType) {
        Timer timer = finiteAnimationTimer(playerOne, animationType);
        if (timer != null) {
            timer.stop();
        }
        setFiniteAnimationTimer(playerOne, animationType, null);
    }

    private Timer finiteAnimationTimer(boolean playerOne, SpriteAnimationType animationType) {
        if (animationType == SpriteAnimationType.DYING) {
            return playerOne ? playerOneDeathTimer : playerTwoDeathTimer;
        }
        return playerOne ? playerOneCelebrationTimer : playerTwoCelebrationTimer;
    }

    private void setFiniteAnimationTimer(boolean playerOne, SpriteAnimationType animationType, Timer timer) {
        if (animationType == SpriteAnimationType.DYING) {
            if (playerOne) {
                playerOneDeathTimer = timer;
            } else {
                playerTwoDeathTimer = timer;
            }
            return;
        }

        if (playerOne) {
            playerOneCelebrationTimer = timer;
        } else {
            playerTwoCelebrationTimer = timer;
        }
    }

    private void stopFightAnimation(boolean playerOne) {
        Timer timer = playerOne ? playerOneFightTimer : playerTwoFightTimer;
        if (timer != null) {
            timer.stop();
        }
        if (playerOne) {
            playerOneFightTimer = null;
        } else {
            playerTwoFightTimer = null;
        }
    }

    private void stopFightAnimations() {
        stopFightAnimation(true);
        stopFightAnimation(false);
    }

    private void stopDeathAnimations() {
        if (playerOneDeathTimer != null) {
            playerOneDeathTimer.stop();
            playerOneDeathTimer = null;
        }
        if (playerTwoDeathTimer != null) {
            playerTwoDeathTimer.stop();
            playerTwoDeathTimer = null;
        }
    }

    private void stopCelebrationAnimations() {
        if (playerOneCelebrationTimer != null) {
            playerOneCelebrationTimer.stop();
            playerOneCelebrationTimer = null;
        }
        if (playerTwoCelebrationTimer != null) {
            playerTwoCelebrationTimer.stop();
            playerTwoCelebrationTimer = null;
        }
    }

    private void stopSpriteAnimations() {
        stopFightAnimations();
        stopDeathAnimations();
        stopCelebrationAnimations();
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
            stopFightAnimations();
            if (!humanPlayer.character().isAlive()) {
                playDyingAnimation(playerOneSpriteLabel, selectedOption(humanSelector), SpriteOrientation.SE, true);
            } else if (!aiPlayer.character().isAlive()) {
                playCelebrationAnimation(playerOneSpriteLabel, selectedOption(humanSelector), SpriteOrientation.SE, true);
            }
            if (!aiPlayer.character().isAlive()) {
                playDyingAnimation(playerTwoSpriteLabel, selectedOption(aiSelector), SpriteOrientation.SW, false);
            } else if (!humanPlayer.character().isAlive()) {
                playCelebrationAnimation(playerTwoSpriteLabel, selectedOption(aiSelector), SpriteOrientation.SW, false);
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
        stopSpriteAnimations();
        playerOneActionPanel.removeAll();
        playerOneActionPanel.revalidate();
        playerOneActionPanel.repaint();
        playerTwoActionPanel.removeAll();
        playerTwoActionPanel.revalidate();
        playerTwoActionPanel.repaint();
        setSpriteIcon(playerOneSpriteLabel, null, "");
        setSpriteIcon(playerTwoSpriteLabel, null, "");
        refreshStartPreviews();
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

    private enum SpriteAnimationType {
        DYING,
        CELEBRATION
    }

    private enum SpriteOrientation {
        SE("se"),
        SW("sw");

        private final String suffix;

        SpriteOrientation(String suffix) {
            this.suffix = suffix;
        }
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

        ImageIcon standingIcon() {
            return loadIcon(label + "/rotations/south.png");
        }

        GifAnimation fightingAnimation(SpriteOrientation orientation) {
            return loadGifAnimation(label + "/animations/fighting_stance_" + orientation.suffix + ".gif");
        }

        GifAnimation dyingAnimation(SpriteOrientation orientation) {
            return loadGifAnimation(label + "/animations/dying_" + orientation.suffix + ".gif");
        }

        GifAnimation celebrationAnimation(SpriteOrientation orientation) {
            return loadGifAnimation(label + "/animations/" + label + "_celebration_" + orientation.suffix + ".gif");
        }

        private ImageIcon loadIcon(String path) {
            if (ICON_CACHE.containsKey(path)) {
                return ICON_CACHE.get(path);
            }

            URL resource = BattleUi.class.getClassLoader().getResource(path);
            ImageIcon icon = null;
            if (resource != null) {
                icon = new ImageIcon(resource);
            } else {
                File resourceFile = new File("src/main/resources", path);
                if (resourceFile.isFile()) {
                    icon = new ImageIcon(resourceFile.getPath());
                }
            }

            if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                ICON_CACHE.put(path, null);
                return null;
            }

            Image scaledImage = icon.getImage().getScaledInstance(
                SPRITE_IMAGE_SIZE,
                SPRITE_IMAGE_SIZE,
                Image.SCALE_FAST
            );
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            ICON_CACHE.put(path, scaledIcon);
            return scaledIcon;
        }

        private GifAnimation loadGifAnimation(String path) {
            URL resource = BattleUi.class.getClassLoader().getResource(path);
            File resourceFile = new File("src/main/resources", path);
            try (ImageInputStream input = resource != null
                ? ImageIO.createImageInputStream(resource.openStream())
                : resourceFile.isFile() ? ImageIO.createImageInputStream(resourceFile) : null) {
                if (input == null) {
                    return GifAnimation.empty();
                }
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                if (!readers.hasNext()) {
                    return GifAnimation.empty();
                }

                ImageReader reader = readers.next();
                try {
                    reader.setInput(input);
                    int frameCount = reader.getNumImages(true);
                    List<ImageIcon> frames = new ArrayList<>();
                    List<Integer> delays = new ArrayList<>();
                    for (int i = 0; i < frameCount; i++) {
                        BufferedImage frame = reader.read(i);
                        frames.add(scaleFrame(frame));
                        delays.add(frameDelayMs(reader.getImageMetadata(i)));
                    }
                    return new GifAnimation(frames, delays);
                } finally {
                    reader.dispose();
                }
            } catch (IOException ignored) {
                return GifAnimation.empty();
            }
        }

        private ImageIcon scaleFrame(BufferedImage frame) {
            Image scaledImage = frame.getScaledInstance(
                SPRITE_IMAGE_SIZE,
                SPRITE_IMAGE_SIZE,
                Image.SCALE_FAST
            );
            return new ImageIcon(scaledImage);
        }

        private int frameDelayMs(IIOMetadata metadata) {
            String metadataFormat = metadata.getNativeMetadataFormatName();
            if (metadataFormat == null) {
                return 100;
            }

            Node root = metadata.getAsTree(metadataFormat);
            Node graphicsControlExtension = findNode(root, "GraphicControlExtension");
            if (graphicsControlExtension == null || graphicsControlExtension.getAttributes() == null) {
                return 100;
            }

            Node delayNode = graphicsControlExtension.getAttributes().getNamedItem("delayTime");
            if (delayNode == null) {
                return 100;
            }

            try {
                int hundredths = Integer.parseInt(delayNode.getNodeValue());
                return Math.max(MIN_GIF_FRAME_DELAY_MS, hundredths * 10);
            } catch (NumberFormatException ignored) {
                return 100;
            }
        }

        private Node findNode(Node node, String name) {
            if (node == null) {
                return null;
            }
            if (name.equals(node.getNodeName())) {
                return node;
            }
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                Node found = findNode(child, name);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class GifAnimation {
        private final List<ImageIcon> frames;
        private final List<Integer> delays;

        private GifAnimation(List<ImageIcon> frames, List<Integer> delays) {
            this.frames = frames;
            this.delays = delays;
        }

        private static GifAnimation empty() {
            return new GifAnimation(new ArrayList<>(), new ArrayList<>());
        }

        private int delayAt(int index) {
            if (index < 0 || index >= delays.size()) {
                return 100;
            }
            return Math.max(MIN_GIF_FRAME_DELAY_MS, (int) Math.round(delays.get(index) / GIF_PLAYBACK_SPEED));
        }
    }
}

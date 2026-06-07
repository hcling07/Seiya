package seiya.server.session;

import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.actions.Defend;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
import seiya.characters.Character;
import seiya.controllers.Controller;
import seiya.game.CharacterType;
import seiya.game.Player;
import seiya.game.RuleSet;
import seiya.game.TurnResolver;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class GameSession {
    private static final Controller NOOP_CONTROLLER = (self, opponent, available) -> available.get(0);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ROOM_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String TOKEN_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final String roomCode;
    private final RuleSet ruleSet;
    private final List<String> log = new ArrayList<>();
    private final PlayerSeat[] seats = new PlayerSeat[2];
    private long lastAccessMillis = System.currentTimeMillis();
    private long endedAtMillis;
    private boolean battleEnded;
    private String resultText;

    public GameSession(String roomCode, RuleSet ruleSet, CharacterType hostCharacter) {
        this.roomCode = roomCode;
        this.ruleSet = ruleSet;
        seats[0] = createSeat(1, hostCharacter);
        log.add("Room " + roomCode + " created. Waiting for Player 2.");
    }

    public static String newRoomCode() {
        return randomString(ROOM_ALPHABET, 5);
    }

    public synchronized JoinResult join(CharacterType characterType) {
        touch();
        if (seats[1] != null) {
            throw new IllegalStateException("Room is full.");
        }

        seats[1] = createSeat(2, characterType);
        log.add("Player 2 joined as " + characterType.label() + ".");
        log.add("Battle started with " + ruleSet + " rules.");
        return new JoinResult(seats[1].token, 2);
    }

    public synchronized JoinResult hostJoinResult() {
        touch();
        return new JoinResult(seats[0].token, 1);
    }

    public synchronized SubmitResult submitAction(String token, String actionId) {
        touch();
        PlayerSeat seat = seatForToken(token);
        ensureActiveBattle();
        if (seat.pendingActionId != null) {
            return new SubmitResult(false, "Action already locked.");
        }

        Action action = actionById(seat.player, actionId);
        if (action == null) {
            return new SubmitResult(false, "Action is not available.");
        }

        seat.pendingActionId = action.name();
        log.add(seat.player.name() + " locked in an action.");

        if (seats[0].pendingActionId != null && seats[1].pendingActionId != null) {
            resolvePendingTurn();
        }
        return new SubmitResult(true, "Action submitted.");
    }

    public synchronized void rematch(String token) {
        seatForToken(token);
        if (seats[1] == null) {
            throw new IllegalStateException("Waiting for Player 2.");
        }

        seats[0].resetPlayer(createPlayer(1, seats[0].characterType));
        seats[1].resetPlayer(createPlayer(2, seats[1].characterType));
        battleEnded = false;
        resultText = null;
        endedAtMillis = 0L;
        log.clear();
        log.add("Rematch started with " + ruleSet + " rules.");
        touch();
    }

    public synchronized boolean exit(String token) {
        PlayerSeat exitingSeat = seatForToken(token);
        PlayerSeat remainingSeat = exitingSeat == seats[0] ? seats[1] : seats[0];
        if (remainingSeat == null) {
            return true;
        }

        seats[0] = remainingSeat;
        seats[0].resetSlot(1);
        seats[0].resetPlayer(createPlayer(1, seats[0].characterType));
        seats[1] = null;
        battleEnded = false;
        resultText = null;
        endedAtMillis = 0L;
        log.clear();
        log.add("Room " + roomCode + " created. Waiting for Player 2.");
        touch();
        return false;
    }

    public synchronized SessionState state(String token) {
        touch();
        int requesterSlot = 0;
        if (token != null && !token.trim().isEmpty()) {
            requesterSlot = seatForToken(token).slot;
        }

        return new SessionState(
            roomCode,
            ruleSet.name(),
            status(),
            requesterSlot,
            battleEnded,
            resultText,
            Arrays.asList(playerState(seats[0], requesterSlot), playerState(seats[1], requesterSlot)),
            new ArrayList<>(log)
        );
    }

    public synchronized boolean hasToken(String token) {
        if (token == null) {
            return false;
        }
        for (PlayerSeat seat : seats) {
            if (seat != null && seat.token.equals(token)) {
                return true;
            }
        }
        return false;
    }

    public String roomCode() {
        return roomCode;
    }

    public synchronized long lastAccessMillis() {
        return lastAccessMillis;
    }

    public synchronized long endedAtMillis() {
        return endedAtMillis;
    }

    public boolean isFull() {
        return seats[1] != null;
    }

    private PlayerSeat createSeat(int slot, CharacterType characterType) {
        return new PlayerSeat(slot, characterType, createPlayer(slot, characterType), randomString(TOKEN_ALPHABET, 32));
    }

    private Player createPlayer(int slot, CharacterType characterType) {
        Player player = new Player("Player " + slot, characterType.create(ruleSet), NOOP_CONTROLLER);
        return player;
    }

    private void ensureActiveBattle() {
        if (seats[1] == null) {
            throw new IllegalStateException("Waiting for Player 2.");
        }
        if (battleEnded) {
            throw new IllegalStateException("Battle has ended.");
        }
    }

    private PlayerSeat seatForToken(String token) {
        for (PlayerSeat seat : seats) {
            if (seat != null && seat.token.equals(token)) {
                return seat;
            }
        }
        throw new IllegalArgumentException("Invalid player token.");
    }

    private Action actionById(Player player, String actionId) {
        for (Action action : player.availableActions()) {
            if (action.name().equals(actionId)) {
                return action;
            }
        }
        return null;
    }

    private void resolvePendingTurn() {
        Action actionOne = actionById(seats[0].player, seats[0].pendingActionId);
        Action actionTwo = actionById(seats[1].player, seats[1].pendingActionId);
        seats[0].pendingActionId = null;
        seats[1].pendingActionId = null;

        if (actionOne == null || actionTwo == null) {
            log.add("A locked action became unavailable. Choose again.");
            return;
        }

        log.addAll(TurnResolver.resolve(seats[0].player, actionOne, seats[1].player, actionTwo));
        checkBattleEnd();
    }

    private void checkBattleEnd() {
        boolean playerOneAlive = seats[0].player.character().isAlive();
        boolean playerTwoAlive = seats[1].player.character().isAlive();
        if (playerOneAlive && playerTwoAlive) {
            return;
        }

        battleEnded = true;
        if (playerOneAlive == playerTwoAlive) {
            resultText = "Result: Draw";
        } else {
            PlayerSeat winner = playerOneAlive ? seats[0] : seats[1];
            resultText = "Winner: " + winner.player.name();
        }
        endedAtMillis = System.currentTimeMillis();
        log.add(resultText);
    }

    private void touch() {
        lastAccessMillis = System.currentTimeMillis();
    }

    private String status() {
        if (battleEnded) {
            return "ENDED";
        }
        if (seats[1] == null) {
            return "WAITING";
        }
        return "ACTIVE";
    }

    private SessionState.PlayerState playerState(PlayerSeat seat, int requesterSlot) {
        if (seat == null) {
            return null;
        }

        Character character = seat.player.character();
        List<Action> actions = battleEnded || seats[1] == null ? Collections.emptyList() : seat.player.availableActions();
        List<SessionState.ActionState> actionStates = new ArrayList<>();
        for (Action action : actions) {
            actionStates.add(actionState(action));
        }
        return new SessionState.PlayerState(
            seat.slot,
            seat.slot == requesterSlot,
            seat.player.name(),
            seat.characterType.label(),
            seat.slot == 1 ? "se" : "sw",
            character.isAlive(),
            seat.pendingActionId != null,
            isWinner(seat),
            character.health(),
            character.maxHealth(),
            character.spirit(),
            character.armorWorn(),
            character.remainingArmor(),
            character.defendPercent(),
            actionStates
        );
    }

    private boolean isWinner(PlayerSeat seat) {
        if (!battleEnded || seat == null || !seat.player.character().isAlive()) {
            return false;
        }
        PlayerSeat opponent = seat.slot == 1 ? seats[1] : seats[0];
        return opponent != null && !opponent.player.character().isAlive();
    }

    private SessionState.ActionState actionState(Action action) {
        return new SessionState.ActionState(
            action.name(),
            action.name(),
            actionCategory(action),
            action.attackValue(),
            action.defenseValue(),
            action.spiritCost()
        );
    }

    private String actionCategory(Action action) {
        if (action instanceof Gather || action instanceof WearArmor || action instanceof Defend) {
            return "General";
        }
        if (action instanceof ConsumableAttack) {
            return "Consumable";
        }
        if (action instanceof Attack || action.attackValue() > 0.0) {
            return "Attack";
        }
        return "General";
    }

    private static String randomString(String alphabet, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    public static final class JoinResult {
        private final String token;
        private final int playerSlot;

        private JoinResult(String token, int playerSlot) {
            this.token = token;
            this.playerSlot = playerSlot;
        }

        public String token() {
            return token;
        }

        public int playerSlot() {
            return playerSlot;
        }
    }

    public static final class SubmitResult {
        private final boolean accepted;
        private final String message;

        private SubmitResult(boolean accepted, String message) {
            this.accepted = accepted;
            this.message = message;
        }

        public boolean accepted() {
            return accepted;
        }

        public String message() {
            return message;
        }
    }

    private static final class PlayerSeat {
        private int slot;
        private final CharacterType characterType;
        private Player player;
        private final String token;
        private String pendingActionId;

        private PlayerSeat(int slot, CharacterType characterType, Player player, String token) {
            this.slot = slot;
            this.characterType = characterType;
            this.player = player;
            this.token = token;
        }

        private void resetPlayer(Player player) {
            this.player = player;
            this.pendingActionId = null;
        }

        private void resetSlot(int slot) {
            this.slot = slot;
        }
    }
}

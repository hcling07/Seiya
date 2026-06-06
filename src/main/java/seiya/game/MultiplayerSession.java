package seiya.game;

import seiya.actions.Action;
import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.actions.Defend;
import seiya.actions.Gather;
import seiya.actions.WearArmor;
import seiya.characters.Character;
import seiya.controllers.Controller;
import seiya.util.NumberFormatter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiplayerSession {
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

    public MultiplayerSession(String roomCode, RuleSet ruleSet, CharacterType hostCharacter) {
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

    public synchronized String stateJson(String token) {
        touch();
        int requesterSlot = 0;
        if (token != null && !token.trim().isEmpty()) {
            requesterSlot = seatForToken(token).slot;
        }

        StringBuilder json = new StringBuilder();
        json.append('{');
        field(json, "roomCode", roomCode).append(',');
        field(json, "ruleSet", ruleSet.name()).append(',');
        field(json, "status", status()).append(',');
        json.append("\"requesterSlot\":").append(requesterSlot).append(',');
        json.append("\"battleEnded\":").append(battleEnded).append(',');
        nullableField(json, "resultText", resultText).append(',');
        json.append("\"players\":[");
        appendSeat(json, seats[0], requesterSlot);
        json.append(',');
        appendSeat(json, seats[1], requesterSlot);
        json.append("],");
        json.append("\"log\":[");
        for (int i = 0; i < log.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            string(json, log.get(i));
        }
        json.append(']');
        json.append('}');
        return json.toString();
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

    private void appendSeat(StringBuilder json, PlayerSeat seat, int requesterSlot) {
        if (seat == null) {
            json.append("null");
            return;
        }

        Character character = seat.player.character();
        json.append('{');
        json.append("\"slot\":").append(seat.slot).append(',');
        json.append("\"you\":").append(seat.slot == requesterSlot).append(',');
        field(json, "playerName", seat.player.name()).append(',');
        field(json, "character", seat.characterType.label()).append(',');
        field(json, "orientation", seat.slot == 1 ? "se" : "sw").append(',');
        json.append("\"alive\":").append(character.isAlive()).append(',');
        json.append("\"pending\":").append(seat.pendingActionId != null).append(',');
        json.append("\"winner\":").append(isWinner(seat)).append(',');
        json.append("\"health\":").append(NumberFormatter.fmt(character.health())).append(',');
        json.append("\"maxHealth\":").append(NumberFormatter.fmt(character.maxHealth())).append(',');
        json.append("\"spirit\":").append(NumberFormatter.fmt(character.spirit())).append(',');
        json.append("\"armorWorn\":").append(character.armorWorn()).append(',');
        json.append("\"remainingArmor\":").append(character.remainingArmor()).append(',');
        json.append("\"defendPercent\":").append(character.defendPercent()).append(',');
        json.append("\"availableActions\":[");
        List<Action> actions = battleEnded || seats[1] == null ? Collections.emptyList() : seat.player.availableActions();
        for (int i = 0; i < actions.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            appendAction(json, actions.get(i));
        }
        json.append(']');
        json.append('}');
    }

    private boolean isWinner(PlayerSeat seat) {
        if (!battleEnded || seat == null || !seat.player.character().isAlive()) {
            return false;
        }
        PlayerSeat opponent = seat.slot == 1 ? seats[1] : seats[0];
        return opponent != null && !opponent.player.character().isAlive();
    }

    private void appendAction(StringBuilder json, Action action) {
        json.append('{');
        field(json, "id", action.name()).append(',');
        field(json, "name", action.name()).append(',');
        field(json, "category", actionCategory(action)).append(',');
        json.append("\"attack\":").append(NumberFormatter.fmt(action.attackValue())).append(',');
        json.append("\"defense\":").append(NumberFormatter.fmt(action.defenseValue())).append(',');
        json.append("\"spiritCost\":").append(NumberFormatter.fmt(action.spiritCost()));
        json.append('}');
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

    private static StringBuilder field(StringBuilder json, String name, String value) {
        string(json, name);
        json.append(':');
        string(json, value);
        return json;
    }

    private static StringBuilder nullableField(StringBuilder json, String name, String value) {
        string(json, name);
        json.append(':');
        if (value == null) {
            json.append("null");
        } else {
            string(json, value);
        }
        return json;
    }

    private static void string(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    json.append("\\\\");
                    break;
                case '"':
                    json.append("\\\"");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        json.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                    break;
            }
        }
        json.append('"');
    }

    public static final class Registry {
        private static final long ENDED_ROOM_TTL_MILLIS = 2L * 60L * 1000L;
        private static final long INACTIVE_ROOM_TTL_MILLIS = 15L * 60L * 1000L;

        private final Map<String, MultiplayerSession> sessions = new ConcurrentHashMap<>();

        public MultiplayerSession create(RuleSet ruleSet, CharacterType hostCharacter) {
            cleanup();
            String roomCode;
            do {
                roomCode = MultiplayerSession.newRoomCode();
            } while (sessions.containsKey(roomCode));

            MultiplayerSession session = new MultiplayerSession(roomCode, ruleSet, hostCharacter);
            sessions.put(roomCode, session);
            return session;
        }

        public MultiplayerSession get(String roomCode) {
            cleanup();
            MultiplayerSession session = sessions.get(roomCode == null ? "" : roomCode.toUpperCase(Locale.ROOT));
            if (session == null) {
                throw new IllegalArgumentException("Room not found.");
            }
            return session;
        }

        public boolean close(String roomCode, String token) {
            MultiplayerSession session = get(roomCode);
            boolean shouldRemove = session.exit(token);
            if (shouldRemove) {
                sessions.remove(session.roomCode());
            }
            return shouldRemove;
        }

        private void cleanup() {
            long now = System.currentTimeMillis();
            for (MultiplayerSession session : sessions.values()) {
                long endedAt = session.endedAtMillis();
                boolean expiredEndedRoom = endedAt > 0L && now - endedAt > ENDED_ROOM_TTL_MILLIS;
                boolean expiredInactiveRoom = now - session.lastAccessMillis() > INACTIVE_ROOM_TTL_MILLIS;
                if (expiredEndedRoom || expiredInactiveRoom) {
                    sessions.remove(session.roomCode());
                }
            }
        }
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

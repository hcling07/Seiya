package seiya.server.session;

import seiya.game.CharacterType;
import seiya.game.RuleSet;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GameSessionRegistry {
    private static final long ENDED_ROOM_TTL_MILLIS = 2L * 60L * 1000L;
    private static final long INACTIVE_ROOM_TTL_MILLIS = 15L * 60L * 1000L;

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    public GameSession create(RuleSet ruleSet, CharacterType hostCharacter) {
        cleanup();
        String roomCode;
        do {
            roomCode = GameSession.newRoomCode();
        } while (sessions.containsKey(roomCode));

        GameSession session = new GameSession(roomCode, ruleSet, hostCharacter);
        sessions.put(roomCode, session);
        return session;
    }

    public GameSession get(String roomCode) {
        cleanup();
        GameSession session = sessions.get(roomCode == null ? "" : roomCode.toUpperCase(Locale.ROOT));
        if (session == null) {
            throw new IllegalArgumentException("Room not found.");
        }
        return session;
    }

    public boolean close(String roomCode, String token) {
        GameSession session = get(roomCode);
        boolean shouldRemove = session.exit(token);
        if (shouldRemove) {
            sessions.remove(session.roomCode());
        }
        return shouldRemove;
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        for (GameSession session : sessions.values()) {
            long endedAt = session.endedAtMillis();
            boolean expiredEndedRoom = endedAt > 0L && now - endedAt > ENDED_ROOM_TTL_MILLIS;
            boolean expiredInactiveRoom = now - session.lastAccessMillis() > INACTIVE_ROOM_TTL_MILLIS;
            if (expiredEndedRoom || expiredInactiveRoom) {
                sessions.remove(session.roomCode());
            }
        }
    }
}

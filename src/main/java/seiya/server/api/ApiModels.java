package seiya.server.api;

import seiya.server.session.SessionState;

import java.util.List;

public final class ApiModels {
    private ApiModels() {
    }

    public static final class CreateRoomRequest {
        private String character;
        private String ruleSet;

        public CreateRoomRequest() {
        }

        public String getCharacter() { return character; }
        public void setCharacter(String character) { this.character = character; }
        public String getRuleSet() { return ruleSet; }
        public void setRuleSet(String ruleSet) { this.ruleSet = ruleSet; }
    }

    public static final class JoinRoomRequest {
        private String character;

        public JoinRoomRequest() {
        }

        public String getCharacter() { return character; }
        public void setCharacter(String character) { this.character = character; }
    }

    public static final class ActionRequest {
        private String token;
        private String action;

        public ActionRequest() {
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    public static final class TokenRequest {
        private String token;

        public TokenRequest() {
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static final class OptionItem {
        private final String id;
        private final String label;

        public OptionItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
    }

    public static final class OptionsResponse {
        private final List<OptionItem> characters;
        private final List<OptionItem> ruleSets;

        public OptionsResponse(List<OptionItem> characters, List<OptionItem> ruleSets) {
            this.characters = characters;
            this.ruleSets = ruleSets;
        }

        public List<OptionItem> getCharacters() { return characters; }
        public List<OptionItem> getRuleSets() { return ruleSets; }
    }

    public static final class JoinResponse {
        private final String roomCode;
        private final String playerToken;
        private final int playerSlot;
        private final SessionState state;

        public JoinResponse(String roomCode, String playerToken, int playerSlot, SessionState state) {
            this.roomCode = roomCode;
            this.playerToken = playerToken;
            this.playerSlot = playerSlot;
            this.state = state;
        }

        public String getRoomCode() { return roomCode; }
        public String getPlayerToken() { return playerToken; }
        public int getPlayerSlot() { return playerSlot; }
        public SessionState getState() { return state; }
    }

    public static final class StateMessageResponse {
        private final String message;
        private final SessionState state;

        public StateMessageResponse(String message, SessionState state) {
            this.message = message;
            this.state = state;
        }

        public String getMessage() { return message; }
        public SessionState getState() { return state; }
    }

    public static final class ExitResponse {
        private final String message;
        private final boolean closed;

        public ExitResponse(String message, boolean closed) {
            this.message = message;
            this.closed = closed;
        }

        public String getMessage() { return message; }
        public boolean isClosed() { return closed; }
    }

    public static final class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}

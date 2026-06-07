package seiya.server.api;

import seiya.game.CharacterType;
import seiya.game.RuleSet;
import seiya.server.session.GameSession;
import seiya.server.session.GameSessionRegistry;
import seiya.server.session.SessionState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GameApi {
    private final GameSessionRegistry sessions;

    public GameApi(GameSessionRegistry sessions) {
        this.sessions = sessions;
    }

    public ApiModels.OptionsResponse options() {
        List<ApiModels.OptionItem> characters = new ArrayList<>();
        for (CharacterType character : CharacterType.values()) {
            characters.add(new ApiModels.OptionItem(character.name(), character.label()));
        }

        List<ApiModels.OptionItem> ruleSets = new ArrayList<>();
        for (RuleSet ruleSet : RuleSet.values()) {
            ruleSets.add(new ApiModels.OptionItem(ruleSet.name(), ruleSet.toString()));
        }
        return new ApiModels.OptionsResponse(characters, ruleSets);
    }

    public ApiModels.JoinResponse createRoom(ApiModels.CreateRoomRequest request) {
        RuleSet ruleSet = parseRuleSet(request.getRuleSet());
        GameSession session = sessions.create(ruleSet, CharacterType.fromLabel(request.getCharacter()));
        return joinResponse(session, session.hostJoinResult());
    }

    public ApiModels.JoinResponse joinRoom(String roomCode, ApiModels.JoinRoomRequest request) {
        GameSession session = sessions.get(roomCode);
        return joinResponse(session, session.join(CharacterType.fromLabel(request.getCharacter())));
    }

    public SessionState state(String roomCode, String token) {
        return sessions.get(roomCode).state(token);
    }

    public ApiModels.StateMessageResponse submitAction(String roomCode, ApiModels.ActionRequest request) {
        GameSession session = sessions.get(roomCode);
        GameSession.SubmitResult result = session.submitAction(request.getToken(), request.getAction());
        if (!result.accepted()) {
            throw new IllegalStateException(result.message());
        }
        return new ApiModels.StateMessageResponse(result.message(), session.state(request.getToken()));
    }

    public ApiModels.StateMessageResponse rematch(String roomCode, ApiModels.TokenRequest request) {
        GameSession session = sessions.get(roomCode);
        session.rematch(request.getToken());
        return new ApiModels.StateMessageResponse("Rematch started.", session.state(request.getToken()));
    }

    public ApiModels.ExitResponse exit(String roomCode, ApiModels.TokenRequest request) {
        boolean closed = sessions.close(roomCode, request.getToken());
        String message = closed ? "Room closed." : "Player exited. Room reset.";
        return new ApiModels.ExitResponse(message, closed);
    }

    private ApiModels.JoinResponse joinResponse(GameSession session, GameSession.JoinResult join) {
        return new ApiModels.JoinResponse(
            session.roomCode(),
            join.token(),
            join.playerSlot(),
            session.state(join.token())
        );
    }

    private RuleSet parseRuleSet(String value) {
        if (value == null || value.trim().isEmpty()) {
            return RuleSet.DEFAULT;
        }
        return RuleSet.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

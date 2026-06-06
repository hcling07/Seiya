package seiya.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiplayerSessionTest {
    @Test
    void resolvesTurnAfterBothPlayersSubmitActions() {
        MultiplayerSession session = new MultiplayerSession("ABCDE", RuleSet.DEFAULT, CharacterType.SEIYA);
        MultiplayerSession.JoinResult playerOne = session.hostJoinResult();
        MultiplayerSession.JoinResult playerTwo = session.join(CharacterType.HYOGA);

        MultiplayerSession.SubmitResult firstSubmit = session.submitAction(playerOne.token(), "Gather");
        MultiplayerSession.SubmitResult secondSubmit = session.submitAction(playerTwo.token(), "Gather");
        String state = session.stateJson(playerOne.token());

        assertTrue(firstSubmit.accepted());
        assertTrue(secondSubmit.accepted());
        assertTrue(state.contains("\"status\":\"ACTIVE\""));
        assertTrue(state.contains("\"spirit\":2"));
        assertTrue(state.contains("Seiya gathered 2.0 spirit."));
        assertTrue(state.contains("Hyoga gathered 2.0 spirit."));
    }

    @Test
    void rematchKeepsPlayersAndResetsBattleState() {
        MultiplayerSession session = new MultiplayerSession("ABCDE", RuleSet.DEFAULT, CharacterType.SEIYA);
        MultiplayerSession.JoinResult playerOne = session.hostJoinResult();
        MultiplayerSession.JoinResult playerTwo = session.join(CharacterType.HYOGA);

        session.submitAction(playerOne.token(), "Gather");
        session.submitAction(playerTwo.token(), "Gather");
        session.rematch(playerOne.token());
        String state = session.stateJson(playerOne.token());

        assertTrue(state.contains("\"status\":\"ACTIVE\""));
        assertTrue(state.contains("\"character\":\"Seiya\""));
        assertTrue(state.contains("\"character\":\"Hyoga\""));
        assertTrue(state.contains("\"spirit\":0"));
        assertTrue(state.contains("Rematch started"));
    }

    @Test
    void registryCloseRemovesRoom() {
        MultiplayerSession.Registry registry = new MultiplayerSession.Registry();
        MultiplayerSession session = registry.create(RuleSet.DEFAULT, CharacterType.SEIYA);
        MultiplayerSession.JoinResult host = session.hostJoinResult();

        boolean closed = registry.close(session.roomCode(), host.token());

        assertTrue(closed);
        assertThrows(IllegalArgumentException.class, () -> registry.get(session.roomCode()));
    }

    @Test
    void registryCloseKeepsRoomForRemainingPlayer() {
        MultiplayerSession.Registry registry = new MultiplayerSession.Registry();
        MultiplayerSession session = registry.create(RuleSet.DEFAULT, CharacterType.SEIYA);
        MultiplayerSession.JoinResult host = session.hostJoinResult();
        MultiplayerSession.JoinResult guest = session.join(CharacterType.HYOGA);

        boolean closed = registry.close(session.roomCode(), host.token());
        String state = registry.get(session.roomCode()).stateJson(guest.token());

        assertTrue(!closed);
        assertTrue(state.contains("\"status\":\"WAITING\""));
        assertTrue(state.contains("\"requesterSlot\":1"));
        assertTrue(state.contains("\"character\":\"Hyoga\""));
        assertTrue(state.contains("Waiting for Player 2"));
    }
}

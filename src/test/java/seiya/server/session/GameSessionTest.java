package seiya.server.session;

import org.junit.jupiter.api.Test;
import seiya.game.CharacterType;
import seiya.game.RuleSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {
    @Test
    void resolvesTurnAfterBothPlayersSubmitActions() {
        GameSession session = new GameSession("ABCDE", RuleSet.DEFAULT, CharacterType.SEIYA);
        GameSession.JoinResult playerOne = session.hostJoinResult();
        GameSession.JoinResult playerTwo = session.join(CharacterType.HYOGA);

        GameSession.SubmitResult firstSubmit = session.submitAction(playerOne.token(), "Gather");
        GameSession.SubmitResult secondSubmit = session.submitAction(playerTwo.token(), "Gather");
        SessionState state = session.state(playerOne.token());

        assertTrue(firstSubmit.accepted());
        assertTrue(secondSubmit.accepted());
        assertEquals("ACTIVE", state.getStatus());
        assertEquals(2.0, state.getPlayers().get(0).getSpirit(), 0.0001);
        assertTrue(state.getLog().contains("Player 1: Seiya gathered 2.0 spirit."));
        assertTrue(state.getLog().contains("Player 2: Hyoga gathered 2.0 spirit."));
    }

    @Test
    void rematchKeepsPlayersAndResetsBattleState() {
        GameSession session = new GameSession("ABCDE", RuleSet.DEFAULT, CharacterType.SEIYA);
        GameSession.JoinResult playerOne = session.hostJoinResult();
        GameSession.JoinResult playerTwo = session.join(CharacterType.HYOGA);

        session.submitAction(playerOne.token(), "Gather");
        session.submitAction(playerTwo.token(), "Gather");
        session.rematch(playerOne.token());
        SessionState state = session.state(playerOne.token());

        assertEquals("ACTIVE", state.getStatus());
        assertEquals("Seiya", state.getPlayers().get(0).getCharacter());
        assertEquals("Hyoga", state.getPlayers().get(1).getCharacter());
        assertEquals(0.0, state.getPlayers().get(0).getSpirit(), 0.0001);
        assertTrue(state.getLog().get(0).contains("Rematch started"));
    }

    @Test
    void registryCloseRemovesRoom() {
        GameSessionRegistry registry = new GameSessionRegistry();
        GameSession session = registry.create(RuleSet.DEFAULT, CharacterType.SEIYA);
        GameSession.JoinResult host = session.hostJoinResult();

        boolean closed = registry.close(session.roomCode(), host.token());

        assertTrue(closed);
        assertThrows(IllegalArgumentException.class, () -> registry.get(session.roomCode()));
    }

    @Test
    void registryCloseKeepsRoomForRemainingPlayer() {
        GameSessionRegistry registry = new GameSessionRegistry();
        GameSession session = registry.create(RuleSet.DEFAULT, CharacterType.SEIYA);
        GameSession.JoinResult host = session.hostJoinResult();
        GameSession.JoinResult guest = session.join(CharacterType.HYOGA);

        boolean closed = registry.close(session.roomCode(), host.token());
        SessionState state = registry.get(session.roomCode()).state(guest.token());

        assertFalse(closed);
        assertEquals("WAITING", state.getStatus());
        assertEquals(1, state.getRequesterSlot());
        assertEquals("Hyoga", state.getPlayers().get(0).getCharacter());
        assertTrue(state.getLog().get(0).contains("Waiting for Player 2"));
    }
}

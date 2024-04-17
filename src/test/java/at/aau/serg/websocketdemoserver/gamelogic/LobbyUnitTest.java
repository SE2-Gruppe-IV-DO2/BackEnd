package at.aau.serg.websocketdemoserver.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LobbyUnitTest {
    Lobby lobby;

    @BeforeEach
    public void initLobby() {
        String testLobbyCode = "TEST_LOBBY";
        lobby = new Lobby(testLobbyCode);
    }

    @Test
    public void testValidLobbyCode() {
        String validCode = "TEST";
        Lobby validLobby = new Lobby(validCode);
        assertEquals(validCode, validLobby.getLobbyCode());
    }

    @Test
    public void testInvalidLobbyCode_Null() {
        assertThrows(IllegalArgumentException.class, () -> new Lobby(null));
    }

    @Test
    public void testInvalidLobbyCode_EmptyString() {
        assertThrows(IllegalArgumentException.class, () -> new Lobby(""));
    }

    @Test
    public void testInvalidLobbyCode_Whitespace() {
        assertThrows(IllegalArgumentException.class, () -> new Lobby("   "));
    }

    @Test
    public void testValidLobbyCode_MixedWhitespace() {
        Lobby lobby = new Lobby("  TEST  ");
        assertEquals("TEST", lobby.getLobbyCode());
    }

    @Test
    public void testAddPlayer_ThrowsIllegalStateException_WhenLobbyIsFull() {
        for (int i = 0; i < Lobby.MAX_PLAYER_COUNT; i++) {
            lobby.addPlayer(new Player("player" + i, "TEST"));
        }
        assertThrows(IllegalStateException.class, () -> lobby.addPlayer(new Player("player5", "TEST")));
    }

    @Test
    public void testIsValid_ReturnsFalse_WhenCodeIsNull() {
        assertFalse(Lobby.isValid(null));
    }

    @Test
    public void testIsValid_ReturnsFalse_WhenCodeIsEmpty() {
        assertFalse(Lobby.isValid("   "));
    }

    @Test
    public void testIsValid_ReturnsTrue_WhenCodeIsValid() {
        assertTrue(Lobby.isValid("VALID_CODE"));
    }

    @Test
    public void testIsReadyToStart_ReturnsFalse_WhenNotEnoughPlayers() {
        lobby.addPlayer(new Player("player1", "TEST"));
        lobby.addPlayer(new Player("player2", "TEST"));
        assertFalse(lobby.isReadyToStart());
    }

    @Test
    public void testIsReadyToStart_ReturnsTrue_WhenEnoughPlayers() {
        Lobby lobby = new Lobby("VALID_CODE");
        for (int i = 0; i < 3; i++) {
            lobby.addPlayer(new Player("player" + i, "TEST"));
        }
        assertTrue(lobby.isReadyToStart());
    }

    @Test
    public void lobbyGameStartedDefaultValueIsFalse() {
        assertFalse(lobby.isLobbyGameStarted());
    }

    @Test
    public void lobbyGameStartedSetterAndGetter() {
        lobby.setLobbyGameStarted(true);
        assertTrue(lobby.isLobbyGameStarted());
    }
}

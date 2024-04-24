package at.aau.serg.websocketdemoserver.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void testGetDeckFromLobby() {
        assertNotNull(lobby.getDeck());
    }

    @Test
    public void testGetActivePlayer() {
        Player player1 = new Player("1", "TEST");
        lobby.addPlayer(player1);

        assertEquals(player1, lobby.getActivePlayer());
    }

    @Test
    public void testEndTurnForMultiplePlayer() {
        Player player1 = new Player("1", "TEST");
        Player player2 = new Player("2", "TEST");
        Player player3 = new Player("3", "TEST");
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);

        lobby.endCurrentPlayersTurn();
        assertEquals(player2, lobby.getActivePlayer());
    }

    @Test
    public void testEndTurnForOneFullRound() {
        Player player1 = new Player("1", "TEST");
        Player player2 = new Player("2", "TEST");
        Player player3 = new Player("3", "TEST");
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);

        lobby.endCurrentPlayersTurn();
        lobby.endCurrentPlayersTurn();
        lobby.endCurrentPlayersTurn();

        assertEquals(player1, lobby.getActivePlayer());
    }

    @Test
    public void testGetActivePlayer_IndexOutOfBounds() throws NoSuchFieldException, IllegalAccessException {
        Player player1 = new Player("1", "TEST");
        lobby.addPlayer(player1);

        // Simulate the case where indexOfActivePlayer is greater than the size of the players list
        int indexOfActivePlayer = 2; // Assuming players list has only 2 elements
        setPrivateField(lobby, "indexOfActivePlayer", indexOfActivePlayer);

        assertThrows(IllegalStateException.class, lobby::getActivePlayer);
    }

    // Helper method to set private fields using reflection
    private void setPrivateField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}

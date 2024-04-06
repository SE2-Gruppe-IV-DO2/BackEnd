package at.aau.serg.websocketdemoserver.gamelogic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LobbyUnitTest {

    @Test
    public void testValidLobbyCode() {
        String validCode = "TEST";
        Lobby lobby = new Lobby(validCode);
        assertEquals(validCode, lobby.lobbyCode());
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
        assertEquals("TEST", lobby.lobbyCode());
    }
}

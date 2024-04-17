package at.aau.serg.websocketdemoserver.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class LobbyManagerUnitTest {
    private LobbyManager lobbyManager;

    @BeforeEach
    public void setUp() {
        lobbyManager = LobbyManager.getInstance();
        lobbyManager.deleteAllLobbies();
    }

    @Test
    public void testCreateLobby() throws Exception {
        String lobbyCode = lobbyManager.createLobby();
        assertNotNull(lobbyCode);
        assertEquals(LobbyManager.LOBBY_CODE_LENGTH, lobbyCode.length());
    }

    @Test
    public void testGetAllLobbies() {
        List<Lobby> lobbies = lobbyManager.getAllLobbies();
        assertNotNull(lobbies);
        assertTrue(lobbies.isEmpty());
    }

    @Test
    public void testUniqueCodesGenerated() throws Exception {
        Set<String> lobbyCodes = lobbyManager.getAllLobbies().stream()
                .map(Lobby::getLobbyCode)
                .collect(Collectors.toSet());
        assertEquals(0, lobbyCodes.size());

        // Create multiple lobbies
        int numLobbies = 5;
        for (int i = 0; i < numLobbies; i++) {
            lobbyManager.createLobby();
        }

        // Check that all generated lobby codes are unique
        lobbyCodes = lobbyManager.getAllLobbies().stream()
                .map(Lobby::getLobbyCode)
                .collect(Collectors.toSet());
        assertEquals(numLobbies, lobbyCodes.size());
    }

    @Test
    public void testSingleton() throws Exception {
        // Use 2 ""different" objects and check if the values in lobbyManager2 are correct
        LobbyManager lobbyManager1 = LobbyManager.getInstance();
        LobbyManager lobbyManager2 = LobbyManager.getInstance();

        lobbyManager1.createLobby();
        String lobbyCode = lobbyManager2.getAllLobbies().get(0).getLobbyCode();
        assertNotNull(lobbyCode);
    }

    @Test
    public void testCreateLobby_MaxRetriesExceeded() throws Exception {
        LobbyManager lobbyManager = LobbyManager.getInstance();

        // Mock the behavior of generateUniqueCode to always return the same code
        LobbyManager mockedLobbyManager = spy(lobbyManager);
        doReturn("TEST12").when(mockedLobbyManager).generateCode();

        mockedLobbyManager.createLobby();

        assertThrows(Exception.class, mockedLobbyManager::createLobby, "Expected exception when max retries are exceeded");
    }

    @Test
    public void testAddUserToLobby_Success() throws Exception {
        String lobbyCode = lobbyManager.createLobby();
        String playerID = "user1";
        String playerName = "USER_NAME";

        lobbyManager.addPlayerToLobby(lobbyCode, playerID, playerName);
        List<Lobby> lobbies = lobbyManager.getAllLobbies();
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyCode().equals(lobbyCode)) {
                assertTrue(lobby.getPlayerIDs().contains(playerID));
            }
        }
    }

    @Test
    public void testAddUserToNonexistentLobby() {
        String lobbyCode = "lobby1";
        String userID = "user1";
        String playerName = "USER_NAME";

        assertThrows(IllegalArgumentException.class, () -> lobbyManager.addPlayerToLobby(lobbyCode, userID, playerName));
    }

    @Test
    public void testAddUserToNullLobbyCode() throws Exception {
        String lobbyCode1 = lobbyManager.createLobby();
        String playerName = "USER_NAME";

        lobbyManager.addPlayerToLobby(lobbyCode1, "player1", playerName);
        lobbyManager.addPlayerToLobby(lobbyCode1, "player2", playerName);
        String lobbyCode = null;
        String userID = "user1";

        assertThrows(IllegalArgumentException.class, () -> lobbyManager.addPlayerToLobby(lobbyCode, userID, playerName));
    }

    @Test
    public void testIsPlayerInLobby_ShouldReturnTrue() throws Exception {
        String lobbyCode1 = lobbyManager.createLobby();
        String playerName = "USER_NAME";
        lobbyManager.addPlayerToLobby(lobbyCode1, "player1", playerName);

        assertTrue(lobbyManager.isPlayerInLobby(lobbyCode1, "player1"));
    }

    @Test
    public void testIsPlayerInLobby_ShouldReturnFalse() {
        assertFalse(lobbyManager.isPlayerInLobby("NOT_EXISTING_LOBBY", "player1"));
    }

    @Test
    public void testIsPlayerInLobby_LobbyDoesNotExist() {
        assertFalse(lobbyManager.isPlayerInLobby("lobby3", "player1"));
    }

    @Test
    public void testIsPlayerInLobby_WithNullLobbyCode() {
        assertFalse(lobbyManager.isPlayerInLobby(null, "player1"));
    }

    @Test
    public void testIsPlayerInLobby_NullPlayerID() {
        assertFalse(lobbyManager.isPlayerInLobby("lobby1", null));
    }
}

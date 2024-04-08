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
                .map(Lobby::lobbyCode)
                .collect(Collectors.toSet());
        assertEquals(0, lobbyCodes.size());

        // Create multiple lobbies
        int numLobbies = 5;
        for (int i = 0; i < numLobbies; i++) {
            lobbyManager.createLobby();
        }

        // Check that all generated lobby codes are unique
        lobbyCodes = lobbyManager.getAllLobbies().stream()
                .map(Lobby::lobbyCode)
                .collect(Collectors.toSet());
        assertEquals(numLobbies, lobbyCodes.size());
    }

    @Test
    public void testSingleton() throws Exception {
        // Use 2 ""different" objects and check if the values in lobbyManager2 are correct
        LobbyManager lobbyManager1 = LobbyManager.getInstance();
        LobbyManager lobbyManager2 = LobbyManager.getInstance();

        lobbyManager1.createLobby();
        String lobbyCode = lobbyManager2.getAllLobbies().get(0).lobbyCode();
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
}

package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import at.aau.serg.websocketdemoserver.messaging.dtos.CardPlayRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void startGameForLobby_LobbyDoesNotExist_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> lobbyManager.startGameForLobby("nonExistentLobby"));
    }

    @Test
    void startGameForLobby_GameAlreadyStarted_ThrowsIllegalStateException() throws Exception {
        String lobbyCode = lobbyManager.createLobby();

        lobbyManager.startGameForLobby(lobbyCode);

        assertThrows(IllegalStateException.class, () -> {
            lobbyManager.startGameForLobby(lobbyCode); // Try starting the game again
        });
    }

    @Test
    void startGameForLobby_GameNotStarted_SuccessfullyStartsGame() throws Exception {
        String lobbyCode = lobbyManager.createLobby();
        lobbyManager.startGameForLobby(lobbyCode);
        assertTrue(lobbyManager.getLobbyByCode(lobbyCode).isLobbyGameStarted());
    }

    @Test
    public void testDealNewRound_withNullLobbyCode() {
        assertThrows(Exception.class, () -> lobbyManager.dealNewRound(null));
    }

    @Test
    public void testDealNewRound_withCorrectLobbyCodeButNoPlayers() throws Exception {
        String lobbyCode = lobbyManager.createLobby();
        assertThrows(Exception.class, () -> lobbyManager.dealNewRound(lobbyCode));
    }

    @Test
    public void testDealNewRound_withCorrectLobbyCodeAndPlayers() throws Exception {
        String lobbyCode = lobbyManager.createLobby();
        lobbyManager.addPlayerToLobby(lobbyCode, new Player("playerID1", "Player1"));
        lobbyManager.addPlayerToLobby(lobbyCode, new Player("playerID2", "Player2"));
        lobbyManager.addPlayerToLobby(lobbyCode, new Player("playerID3", "Player3"));

        assertDoesNotThrow(() -> lobbyManager.dealNewRound(lobbyCode));
    }

    @Test
    void testCardPlayedSuccess() {
        String lobbyCode;
        try {
            lobbyCode = lobbyManager.createLobby();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Player player = new Player("123", "John");
        lobbyManager.addPlayerToLobby(lobbyCode, player);
        Card card = new Card(CardType.BLUE, 3);
        player.getCardsInHand().add(card);
        CardPlayRequest cardPlayRequest = new CardPlayRequest();
        cardPlayRequest.setValue(3);
        cardPlayRequest.setLobbyCode(lobbyCode);
        cardPlayRequest.setPlayerID(player.getPlayerID());
        cardPlayRequest.setColor("blue");

        Card returnCard = lobbyManager.cardPlayed(cardPlayRequest);

        assertEquals(0, player.getCardsInHand().size());
        try {
            assertTrue(lobbyManager.getLobbyByCode(lobbyCode).getCurrentTrick().contains(card));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(card.getCardType(), returnCard.getCardType());
        Assertions.assertEquals(card.getColor(), returnCard.getColor());
        Assertions.assertEquals(card.getValue(), returnCard.getValue());
    }

    @Test
    void testCardPlayedFailureLobby() {
        CardPlayRequest cardPlayRequest = new CardPlayRequest();
        cardPlayRequest.setValue(3);
        cardPlayRequest.setLobbyCode("123");
        cardPlayRequest.setPlayerID("123");
        cardPlayRequest.setColor("blue");

        Assertions.assertThrows(IllegalArgumentException.class, () -> lobbyManager.cardPlayed(cardPlayRequest));
    }

    @Test
    void testCardPlayedFailurePlayer() {
        String lobbyCode;
        try {
            lobbyCode = lobbyManager.createLobby();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CardPlayRequest cardPlayRequest = new CardPlayRequest();
        cardPlayRequest.setValue(3);
        cardPlayRequest.setLobbyCode(lobbyCode);
        cardPlayRequest.setPlayerID("123");
        cardPlayRequest.setColor("blue");


        Assertions.assertThrows(IllegalArgumentException.class, () -> lobbyManager.cardPlayed(cardPlayRequest));
    }

    @Test
    void testCardPlayedFailureCard() {
        String lobbyCode;
        try {
            lobbyCode = lobbyManager.createLobby();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Player player = new Player("123", "John");
        lobbyManager.addPlayerToLobby(lobbyCode, player);
        CardPlayRequest cardPlayRequest = new CardPlayRequest();
        cardPlayRequest.setValue(3);
        cardPlayRequest.setLobbyCode(lobbyCode);
        cardPlayRequest.setPlayerID(player.getPlayerID());
        cardPlayRequest.setColor("blue");

        Assertions.assertThrows(IllegalArgumentException.class, () -> lobbyManager.cardPlayed(cardPlayRequest));
    }

    @Test
    public void testEndCurrentPlayersTurnForLobby() throws Exception {
        String lobbyCode = lobbyManager.createLobby();
        Player player1 = new Player("playerID1", "Player1");
        Player player2 = new Player("playerID2", "Player2");
        lobbyManager.addPlayerToLobby(lobbyCode, player1);
        lobbyManager.addPlayerToLobby(lobbyCode, player2);

        lobbyManager.endCurrentPlayersTurnForLobby(lobbyCode);
        assertEquals(player2.getPlayerID(), lobbyManager.getActivePlayerForLobby(lobbyCode));
    }
}

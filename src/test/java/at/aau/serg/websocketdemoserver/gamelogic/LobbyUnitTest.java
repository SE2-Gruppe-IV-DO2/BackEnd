package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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

    @Test
    void testGetPlayerByIDSuccess() {
        Player p = new Player("player1", "TEST");
        lobby.addPlayer(p);
        assertEquals(p, lobby.getPlayerByID("player1"));
    }

    @Test
    void testGetPLayerByIDFailure() {
        Player p = new Player("player1", "TEST");
        lobby.addPlayer(p);
        assertNull(lobby.getPlayerByID("1"));
    }

    @Test
    void testIfCurrentTrickIsDone_True() {
        Player p = new Player("player1", "TEST");
        lobby.addPlayer(p);
        Player p2 = new Player("player2", "TEST");
        lobby.addPlayer(p2);
        Player p3 = new Player("player2", "TEST");
        lobby.addPlayer(p3);

        Card c1 = new Card(CardType.GREEN, 2);
        Card c2 = new Card(CardType.RED, 2);
        Card c3 = new Card(CardType.BLUE, 2);

        lobby.getCurrentTrick().add(c1);
        lobby.getCurrentTrick().add(c2);
        lobby.getCurrentTrick().add(c3);

        assertTrue(lobby.isCurrentTrickDone());
    }

    @Test
    void testIfCurrentTrickIsDone_False() {
        Player p = new Player("player1", "TEST");
        lobby.addPlayer(p);
        Player p2 = new Player("player2", "TEST");
        lobby.addPlayer(p2);
        Player p3 = new Player("player2", "TEST");
        lobby.addPlayer(p3);

        Card c1 = new Card(CardType.GREEN, 2);
        Card c2 = new Card(CardType.RED, 2);

        lobby.getCurrentTrick().add(c1);
        lobby.getCurrentTrick().add(c2);

        assertFalse(lobby.isCurrentTrickDone());
    }

    @Test
    void testAddCardToTrick() {
        Player p = new Player("player1", "TEST");
        lobby.addPlayer(p);
        Player p2 = new Player("player2", "TEST");
        lobby.addPlayer(p2);

        Card c1 = new Card(CardType.GREEN, 2);
        lobby.addCardToTrick(p.getPlayerID(), c1);

        assertEquals(lobby.getCurrentTrick().size(), 1);
        assertEquals(lobby.getLastPlayedCardPerPlayer().size(), 1);
        assertEquals(c1, lobby.getCurrentTrick().get(0));
        assertEquals(c1, lobby.getLastPlayedCardPerPlayer().get(p.getPlayerID()));
    }

    @Test
    void testClearTrick() {
        Player p = new Player("player1", "TEST");
        lobby.addPlayer(p);
        Player p2 = new Player("player2", "TEST");
        lobby.addPlayer(p2);

        Card c1 = new Card(CardType.GREEN, 2);
        lobby.addCardToTrick(p.getPlayerID(), c1);

        assertEquals(lobby.getCurrentTrick().size(), 1);
        assertEquals(lobby.getLastPlayedCardPerPlayer().size(), 1);

        lobby.clearTrick();

        assertEquals(lobby.getCurrentTrick().size(), 0);
        assertEquals(lobby.getLastPlayedCardPerPlayer().size(), 0);
    }

    @Test
    void testEvaluateAndHandoutTrick() {
        Player p = new Player("player1", "TEST");
        lobby.addPlayer(p);
        Player p2 = new Player("player2", "TEST");
        lobby.addPlayer(p2);

        Card c1 = new Card(CardType.GREEN, 2);
        lobby.addCardToTrick(p.getPlayerID(), c1);

        assertEquals(lobby.getCurrentTrick().size(), 1);
        assertEquals(lobby.getLastPlayedCardPerPlayer().size(), 1);

        lobby.evaluateAndHandoutTrick();

        // Nach dem Zuteilen sollte der Lobby Stapel leer sein und der Spieler die gewonnene Karte haben
        assertEquals(lobby.getCurrentTrick().size(), 0);
        assertEquals(lobby.getLastPlayedCardPerPlayer().size(), 0);
        assertEquals(p.getClaimedTricks().size(), 1);
    }

    @Test
    public void testSetPlayerAsActivePlayer_PlayerFound() {
        lobby.addPlayer(new Player("player1", "test"));
        lobby.addPlayer(new Player("player2", "test"));
        lobby.addPlayer(new Player("player3", "test"));

        lobby.setPlayerAsActivePlayer("player2");

        assertEquals(1, lobby.getIndexOfActivePlayer());
    }

    @Test
    public void testSetPlayerAsActivePlayer_PlayerNotFound() {
        lobby.addPlayer(new Player("player1", "test"));
        lobby.addPlayer(new Player("player2", "test"));
        lobby.addPlayer(new Player("player3", "test"));

        assertThrows(IllegalStateException.class, () -> lobby.setPlayerAsActivePlayer("player4"));
    }

    @Test
    void isRoundFinishedSuccess() {
        lobby.addPlayer(new Player("player1", "test1"));
        lobby.addPlayer(new Player("player2", "test2"));
        lobby.addPlayer(new Player("player3", "test3"));

        Map<String, HashMap<Integer, Integer>> playerPoints = new HashMap<>();
        playerPoints.put("test1", new HashMap<>());
        playerPoints.put("test2", new HashMap<>());
        playerPoints.put("test3", new HashMap<>());

        lobby.setPlayerPoints(playerPoints);

        lobby.getDeck().dealNewRound(lobby.getPlayers());

        for (Player p : lobby.getPlayers()) {
            p.getCardsInHand().clear();
        }

        Assertions.assertTrue(lobby.isRoundFinished());
    }

    @Test
    void isRoundFinishedFailure() {
        lobby.addPlayer(new Player("player1", "test"));
        lobby.addPlayer(new Player("player2", "test"));
        lobby.addPlayer(new Player("player3", "test"));

        lobby.getDeck().dealNewRound(lobby.getPlayers());

        Assertions.assertFalse(lobby.isRoundFinished());
    }

    @Test
    void testCreatePointBoard() {
        lobby.addPlayer(new Player("player1", "test1"));
        lobby.addPlayer(new Player("player2", "test2"));
        lobby.addPlayer(new Player("player3", "test3"));

        Map<String, HashMap<Integer, Integer>> expected = new HashMap<>();
        expected.put("test1", new HashMap<>());
        expected.put("test2", new HashMap<>());
        expected.put("test3", new HashMap<>());

        lobby.createPointBoard();

        Assertions.assertEquals(expected, lobby.getPlayerPoints());
    }

    @Test
    void testSetPointsBoard() {
        lobby.addPlayer(new Player("player1", "test1"));
        lobby.addPlayer(new Player("player2", "test2"));
        lobby.addPlayer(new Player("player3", "test3"));

        for (Player p : lobby.getPlayers()) {
            p.getClaimedTricks().put(CardType.GREEN, 1);
        }

        lobby.createPointBoard();
        lobby.calculateAndSetRoundPoints();

        Map<String, HashMap<Integer, Integer>> expected = new HashMap<>();
        expected.put("test1", new HashMap<>());
        expected.get("test1").put(1,1);
        expected.put("test2", new HashMap<>());
        expected.get("test2").put(1,1);
        expected.put("test3", new HashMap<>());
        expected.get("test3").put(1,1);

        Assertions.assertEquals(expected, lobby.getPlayerPoints());
    }
}

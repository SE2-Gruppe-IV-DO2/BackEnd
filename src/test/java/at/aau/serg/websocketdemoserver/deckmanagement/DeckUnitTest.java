package at.aau.serg.websocketdemoserver.deckmanagement;

import at.aau.serg.websocketdemoserver.gamelogic.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeckUnitTest {

    private Deck deck;
    List<Player> playerList;

    @BeforeEach
    public void initDeck() {
        deck = new Deck();
        playerList = new ArrayList<>();
        playerList.add(new Player("playerID1", "Player1"));
        playerList.add(new Player("playerID2", "Player2"));
        playerList.add(new Player("playerID3", "Player3"));
    }

    @Test
    public void test_initialization_size() {
        assertEquals(64, deck.getDeck().size());
    }

    @Test
    public void test_dealing_numOfCards() {
        deck.dealNewRound(playerList);
        assertEquals(15, playerList.get(0).getCardsInHand().size());

        playerList.add(new Player("playerId4", "Player4"));
        deck.dealNewRound(playerList);
        assertEquals(14, playerList.get(0).getCardsInHand().size());

        playerList.add(new Player("playerId5", "Player5"));
        deck.dealNewRound(playerList);
        assertEquals(13, playerList.get(0).getCardsInHand().size());
    }

    @Test
    public void test_dealing_exception_tooManyPlayers() {
        playerList.add(new Player("playerId4", "Player4"));
        playerList.add(new Player("playerId4", "Player5"));
        playerList.add(new Player("playerId6", "Player6"));
        assertThrows(Exception.class, () -> deck.dealNewRound(playerList));
    }

    @Test
    public void test_dealing_gaia(){
        deck.dealNewRound(playerList);
        boolean gaiaFound = false;
        for (Player player : playerList) {
            if (player.getCardsInHand().stream().anyMatch(card -> card.cardType.equals(CardType.GAIA))) {
                gaiaFound = true;
                break;
            }
        }
        assertTrue(gaiaFound);
    }

    @Test
    public void test_card_toString() {
        Card card = new Card(CardType.BLUE, 123);
        assertEquals("blue 123", card.toString());
    }
}

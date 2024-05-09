package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static at.aau.serg.websocketdemoserver.deckmanagement.CardType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class PlayerUnitTest {
    @Test
    void testConstructorAndGetters() {
        String playerID = "123";
        String playerName = "John";

        Player player = new Player(playerID, playerName);

        assertEquals(playerID, player.getPlayerID());
        assertEquals(playerName, player.getPlayerName());
    }

    @Test
    void testSetter() {
        Player player = new Player("123", "John");

        String newName = "Mike";
        player.setPlayerName(newName);

        assertEquals(newName, player.getPlayerName());
    }

    @Test
    void testPlayCardCardForPlayerFound() {
        Player player = new Player("123", "John");
        Card card1 = new Card(RED, 5);
        Card card2 = new Card(BLUE, 3);
        player.getCardsInHand().add(card1);
        player.getCardsInHand().add(card2);

        Card playedCard = player.playCardForPlayer("red", 5);

        assertEquals(card1, playedCard);
        assertEquals(1, player.getCardsInHand().size());
    }

    @Test()
    void testPlayCardCardForPlayerNotFound() {
        Player player = new Player("123", "John");
        Card card1 = new Card(RED, 2);
        Card card2 = new Card(BLUE, 3);
        player.getCardsInHand().add(card1);
        player.getCardsInHand().add(card2);

        Assertions.assertThrows(IllegalArgumentException.class, () -> player.playCardForPlayer("green", 5));

    }
}

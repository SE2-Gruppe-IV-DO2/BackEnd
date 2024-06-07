package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static at.aau.serg.websocketdemoserver.deckmanagement.CardType.*;
import static org.junit.jupiter.api.Assertions.*;


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

        Card playedCard = player.playCardForPlayer("red", "red", 5);

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

        Assertions.assertThrows(IllegalArgumentException.class, () -> player.playCardForPlayer("green", "green", 5));
    }

    @Test()
    void testPlayerHasGaia() {
        Player player = new Player("123", "John");
        Card gaiaCard = new Card(GAIA, 0);
        Card card2 = new Card(BLUE, 3);
        Card card3 = new Card(GREEN, 3);

        player.getCardsInHand().add(card2);
        player.getCardsInHand().add(gaiaCard);
        player.getCardsInHand().add(card3);

        assertTrue(player.hasGaiaCard());
    }

    @Test()
    void testPlayerHasNoGaia() {
        Player player = new Player("123", "John");
        Card card2 = new Card(BLUE, 3);
        Card card3 = new Card(GREEN, 3);

        player.getCardsInHand().add(card2);
        player.getCardsInHand().add(card3);

        assertFalse(player.hasGaiaCard());
    }

    @Test
    void testPlayGaiaForPlayerFound() {
        Player player = new Player("123", "John");
        Card card1 = new Card(RED, 5);
        Card card2 = new Card(GAIA, 0);
        player.getCardsInHand().add(card1);
        player.getCardsInHand().add(card2);

        Card playedCard = player.playCardForPlayer("gaia", "red", 0);

        assertEquals(card2, playedCard);
        assertEquals(1, player.getCardsInHand().size());
    }

    @Test
    void testAddSingleValueCardsTrick() {
        Player player = new Player("123", "John");
        Card card1 = new Card(CardType.RED, 5);
        Card card2 = new Card(CardType.BLUE, 2);
        List<Card> trickCards = List.of(card1, card2);

        boolean isDead = player.addClaimedTrick(trickCards);

        assertFalse(isDead);
        assertEquals(5, player.getClaimedTricks().get(CardType.RED));
        assertEquals(2, player.getClaimedTricks().get(CardType.BLUE));
    }

    @Test
    void testAddMultipleValueCardsTrick() {
        Player player = new Player("123", "John");
        Card card1 = new Card(CardType.RED, 5);
        Card card2 = new Card(CardType.RED, 7);
        Card card3 = new Card(CardType.RED, 3);
        Card card4 = new Card(CardType.RED, 4);

        Card card5 = new Card(CardType.BLUE, 2);
        Card card6 = new Card(CardType.BLUE, 4);

        List<Card> trickCards = List.of(card1, card2, card3, card4, card5, card6);

        boolean isDead = player.addClaimedTrick(trickCards);

        assertFalse(isDead);
        assertEquals(3, player.getClaimedTricks().get(CardType.RED));
        assertEquals(2, player.getClaimedTricks().get(CardType.BLUE));
    }

    @Test
    void testUpdateClaimedTrickForCardType() {
        Player player = new Player("123", "John");
        player.updateClaimedTrickForCardType(CardType.RED, 5);

        assertEquals(5, player.getClaimedTricks().get(CardType.RED));
    }

    @Test
    void testIsPlayerDead() {
        Player player = new Player("123", "John");
        player.updateClaimedTrickForCardType(CardType.GREEN, 1);
        player.updateClaimedTrickForCardType(CardType.RED, 1);
        player.updateClaimedTrickForCardType(CardType.PURPLE, 1);
        player.updateClaimedTrickForCardType(CardType.BLUE, 1);
        player.updateClaimedTrickForCardType(CardType.YELLOW, 1);

        assertTrue(player.isPlayerDead());
    }

    @Test
    void testIfPlayerIsDeadWithSickle() {
        Player player = new Player("123", "John");
        player.updateClaimedTrickForCardType(CardType.GREEN, 1);
        player.updateClaimedTrickForCardType(CardType.RED, 1);
        player.updateClaimedTrickForCardType(CardType.PURPLE, 1);
        player.updateClaimedTrickForCardType(CardType.BLUE, 1);
        player.updateClaimedTrickForCardType(CardType.YELLOW, 1);
        player.updateClaimedTrickForCardType(GOLDEN_SICKLE, 0);

        assertTrue(player.isPlayerDead());
    }

    @Test
    void testGetHighestValueClaimedTrickType() {
        Player player = new Player("123", "John");
        player.updateClaimedTrickForCardType(CardType.RED, 5);
        player.updateClaimedTrickForCardType(CardType.BLUE, 3);
        player.updateClaimedTrickForCardType(GREEN, 3);

        assertEquals(CardType.RED, player.getHighestValueClaimedTrickType());
    }

    @Test
    void testAddMultipleTricks() {
        Player player = new Player("123", "John");
        Card card1 = new Card(CardType.RED, 5);
        Card card2 = new Card(CardType.BLUE, 2);
        List<Card> trickCards = List.of(card1, card2);
        player.addClaimedTrick(trickCards);

        Card card3 = new Card(CardType.RED, 3);
        Card card4 = new Card(CardType.BLUE, 6);
        List<Card> trickCards2 = List.of(card3, card4);
        boolean isDead = player.addClaimedTrick(trickCards2);

        assertFalse(isDead);
        assertEquals(3, player.getClaimedTricks().get(CardType.RED));
        assertEquals(6, player.getClaimedTricks().get(CardType.BLUE));
    }

    @Test
    void testAddSickleToTrick() {
        Player player = new Player("123", "John");
        Card card1 = new Card(CardType.RED, 5);
        Card card2 = new Card(CardType.BLUE, 2);
        Card sickleCard = new Card(GOLDEN_SICKLE, 0);

        List<Card> trickCards = List.of(card1, card2, sickleCard);
        player.addClaimedTrick(trickCards);

        assertEquals(0, player.getClaimedTricks().get(CardType.RED));
        assertEquals(2, player.getClaimedTricks().get(CardType.BLUE));
    }

    @Test
    void testAddMultipleSickleToTrick() {
        Player player = new Player("123", "John");
        Card card1 = new Card(CardType.RED, 5);
        Card card2 = new Card(CardType.BLUE, 2);
        Card card3 = new Card(GREEN, 3);
        Card card4 = new Card(YELLOW, 4);
        Card sickleCard = new Card(GOLDEN_SICKLE, 0);
        Card sickleCard2 = new Card(GOLDEN_SICKLE, 0);

        List<Card> trickCards = List.of(card1, card2, card3, card4, sickleCard, sickleCard2);
        player.addClaimedTrick(trickCards);

        assertEquals(0, player.getClaimedTricks().get(CardType.RED));
        assertEquals(0, player.getClaimedTricks().get(YELLOW));
        assertEquals(3, player.getClaimedTricks().get(GREEN));
        assertEquals(2, player.getClaimedTricks().get(CardType.BLUE));
    }
}


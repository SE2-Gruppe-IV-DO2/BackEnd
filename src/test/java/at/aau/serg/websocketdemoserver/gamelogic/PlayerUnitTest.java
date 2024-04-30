package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerUnitTest {
    @Mock
    private Card mockedCard;
    @Test
    public void testConstructorAndGetters() {
        String playerID = "123";
        String playerName = "John";

        Player player = new Player(playerID, playerName);

        assertEquals(playerID, player.getPlayerID());
        assertEquals(playerName, player.getPlayerName());
    }

    @Test
    public void testSetter() {
        Player player = new Player("123", "John");

        String newName = "Mike";
        player.setPlayerName(newName);

        assertEquals(newName, player.getPlayerName());
    }

    @Test
    public void testPlayCard_RemovesCardFromHand() {
        Player player = new Player("123", "John");
        mockedCard = mock(Card.class);
        when(mockedCard.getColor()).thenReturn("Red");
        when(mockedCard.getValue()).thenReturn(5);

        List<Card> cardsInHand = new ArrayList<>();
        cardsInHand.add(mockedCard);
        player.setCardsInHand(cardsInHand);

        player.playCard("Red", 5);

        assertEquals(0, player.getCardsInHand().size());
    }

    @Test
    public void testPlayCard_DoesNotRemoveNonexistentCard() {
        Player player = new Player("123", "John");
        mockedCard = mock(Card.class);
        when(mockedCard.getColor()).thenReturn("Blue");
        when(mockedCard.getValue()).thenReturn(3);

        List<Card> cardsInHand = new ArrayList<>();
        cardsInHand.add(mockedCard);
        player.setCardsInHand(cardsInHand);

        player.playCard("Red", 5);

        assertEquals(1, player.getCardsInHand().size());
    }

}

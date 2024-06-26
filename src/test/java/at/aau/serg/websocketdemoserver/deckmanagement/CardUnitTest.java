package at.aau.serg.websocketdemoserver.deckmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CardUnitTest {

    @Test
    void testConstructor1() {
        Card c = new Card("green", 1);

        Assertions.assertEquals(new Card(CardType.GREEN,1).getCardType(), c.getCardType());
        Assertions.assertEquals(new Card(CardType.GREEN,1).getColor(), c.getColor());
        Assertions.assertEquals(new Card(CardType.GREEN,1).getValue(), c.getValue());
    }
}

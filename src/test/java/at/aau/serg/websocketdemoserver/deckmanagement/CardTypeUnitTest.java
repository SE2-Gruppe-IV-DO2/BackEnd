package at.aau.serg.websocketdemoserver.deckmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CardTypeUnitTest {
    @Test
    void testGetCardTypeByColor() {
        Assertions.assertEquals(CardType.GREEN, CardType.getByColor("green"));
    }

    @Test
    void testGetCardTypeByName() {
        Assertions.assertEquals(CardType.BLUE, CardType.getByName("BLUE"));
    }
}

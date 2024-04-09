package at.aau.serg.websocketdemoserver.gamelogic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerUnitTest {
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
}

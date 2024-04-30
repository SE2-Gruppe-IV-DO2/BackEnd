package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Player {
    private final String playerID;
    @Setter
    private String playerName;
    @Setter
    List<Card> cardsInHand = new ArrayList<>();

    public Player(String playerID, String playerName) {
        this.playerID = playerID;
        this.playerName = playerName;
    }

    public void playCard(String color, Integer value) {
        getCardsInHand().removeIf(card ->
                card.getColor().equals(color) && Objects.equals(card.getValue(), value));
    }
}

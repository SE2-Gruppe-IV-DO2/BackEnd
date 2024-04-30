package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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

    public Card playCard(String color, Integer value) {
        for (Iterator<Card> iterator = cardsInHand.iterator(); iterator.hasNext();) {
            Card card = iterator.next();
            if (color.equals(card.getColor()) && value.equals(card.getValue())) {
                iterator.remove();
                return card;
            }
        }
        throw new IllegalArgumentException("Card not found in player's hand");
    }
}

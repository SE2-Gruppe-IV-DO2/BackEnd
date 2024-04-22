package at.aau.serg.websocketdemoserver.deckmanagement;

import at.aau.serg.websocketdemoserver.gamelogic.Player;
import lombok.Data;

import java.util.*;

import static at.aau.serg.websocketdemoserver.deckmanagement.CardType.*;

@Data
public class Deck {

    static final int MAX_CARD_VALUE = 12;

    List<Card> deck;

    public Deck(){
        deck = new ArrayList<>();
        initializeDeck();
    }

    void initializeDeck(){
        for (CardType type : List.of(GREEN, YELLOW, RED, BLUE, PURPLE)){
            for (int i = 1; i <= MAX_CARD_VALUE; i++) {
                deck.add(new Card(type, i));
            }
        }

        deck.add(new Card(GOLDEN_SICKLE, 0));
        deck.add(new Card(GOLDEN_SICKLE, 0));
        deck.add(new Card(MISTLETOE, 0));
        deck.add(new Card(MISTLETOE, 0));
    }

    public void dealNewRound(List<Player> players) {
        int cardsPerPlayer = (18 - players.size());
        List<Card> deckInPlay = getShuffledSubsetWithAddedGaia(cardsPerPlayer * players.size());

        for (Player player : players) {
            player.setCardsInHand(new ArrayList<>());
            for (int i = 0; i < cardsPerPlayer; i++) {
                if (deckInPlay.isEmpty()){
                    throw new IllegalArgumentException("Deck is empty!");
                }
                player.getCardsInHand().add(deckInPlay.remove(0));
            }
        }
    }

    List<Card> getShuffledSubsetWithAddedGaia(int numOfCards) {
        if (numOfCards > size() + 1){
            throw new IllegalArgumentException("Number of Cards not supported by deck");
        }

        // shuffle and take subset>
        Collections.shuffle(deck);
        List<Card> subset = new ArrayList<>(deck.subList(0, numOfCards - 1).stream().map(Card::new).toList());

        // add gaia and shuffle again
        subset.add(new Card(CardType.GAIA, 0));
        Collections.shuffle(subset);

        return subset;
    }

    int size() {
        return deck.size();
    }
}

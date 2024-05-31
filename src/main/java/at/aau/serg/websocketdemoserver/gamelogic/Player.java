package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


@Getter
public class Player {
    private final String playerID;
    @Setter
    private String playerName;
    @Setter
    List<Card> cardsInHand = new ArrayList<>();

    HashMap<CardType, Integer> claimedTricks = new HashMap<CardType, Integer>();

    public Player(String playerID, String playerName) {
        this.playerID = playerID;
        this.playerName = playerName;
    }

    public Card playCardForPlayer(String color, Integer value) {
        for (Iterator<Card> iterator = cardsInHand.iterator(); iterator.hasNext();) {
            Card card = iterator.next();
            if (color.equals(card.getColor()) && value.equals(card.getValue())) {
                iterator.remove();
                return card;
            }
        }
        throw new IllegalArgumentException("Card not found in player's hand");
    }

    public boolean hasGaiaCard() {
        for (Card card : cardsInHand) {
            if (card.getCardType().equals(CardType.GAIA))
                return true;
        }
        return false;
    }

    public boolean addClaimedTrick(List<Card> trickCards) {
        HashMap<CardType, Integer> lowestCardsInTrick = new HashMap<CardType, Integer>();
        int goldenSickleCounter = 0;

        for (Card trickCard : trickCards) {
            CardType currentTrickCardType = trickCard.getCardType();
            int currentTrickCardValue = trickCard.getValue();

            if (currentTrickCardType.equals(CardType.GOLDEN_SICKLE))
                goldenSickleCounter++;
            else {
                if (lowestCardsInTrick.get(currentTrickCardType) == null
                        || currentTrickCardValue < lowestCardsInTrick.getOrDefault(currentTrickCardType, 0))
                    lowestCardsInTrick.put(currentTrickCardType, currentTrickCardValue);
            }
        }

        // update player trick
        updateClaimedTrickForCardType(CardType.GREEN, lowestCardsInTrick.get(CardType.GREEN));
        updateClaimedTrickForCardType(CardType.RED, lowestCardsInTrick.get(CardType.RED));
        updateClaimedTrickForCardType(CardType.PURPLE, lowestCardsInTrick.get(CardType.PURPLE));
        updateClaimedTrickForCardType(CardType.BLUE, lowestCardsInTrick.get(CardType.BLUE));
        updateClaimedTrickForCardType(CardType.YELLOW, lowestCardsInTrick.get(CardType.YELLOW));

        // check for death
        boolean isPlayerDead = isPlayerDead();

        // check for GOLDEN_SICKLE and remove X colours
        if (!isPlayerDead && goldenSickleCounter > 0) {
            for (int i = 0; i < goldenSickleCounter; i++) {
                CardType cardTypeToReset = getHighestValueClaimedTrickType();
                claimedTricks.put(cardTypeToReset, 0);
            }
        }

        return isPlayerDead;
    }

    public void updateClaimedTrickForCardType(CardType cardType, Integer newValue) {
        if (newValue != null && newValue > 0)
            claimedTricks.put(cardType, newValue);
    }

    public boolean isPlayerDead() {
        return claimedTricks.getOrDefault(CardType.GREEN, 0) > 0 && claimedTricks.getOrDefault(CardType.RED, 0) > 0
                && claimedTricks.getOrDefault(CardType.PURPLE, 0) > 0 && claimedTricks.getOrDefault(CardType.BLUE, 0) > 0
                && claimedTricks.getOrDefault(CardType.YELLOW, 0) > 0;
    }

    public CardType getHighestValueClaimedTrickType() {
        CardType maxCardType = null;
        int maxValue = Integer.MIN_VALUE;

        for (Map.Entry<CardType, Integer> entry : claimedTricks.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxCardType = entry.getKey();
            }
        }

        return maxCardType;
    }
}

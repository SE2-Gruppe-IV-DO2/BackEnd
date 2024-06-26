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
    @Setter
    List<Card> claimedTricks = new ArrayList<>();
    boolean cheatedInCurrentRound = false;

    public Player(String playerID, String playerName) {
        this.playerID = playerID;
        this.playerName = playerName;
    }

    public Card playCardForPlayer(String name, String color, Integer value) {
        for (Iterator<Card> iterator = cardsInHand.iterator(); iterator.hasNext();) {
            Card card = iterator.next();
            if (color.equals(card.getColor()) && value.equals(card.getValue())) {
                iterator.remove();
                return card;
            }
            // case of gaia, mistletoe, golden sickle
            if (name.equals(card.getCardType().getName()) && value.equals(card.getValue())) {
                iterator.remove();
                card.setColor(color);
                return card;
            }
        }
        throw new IllegalArgumentException("Card not found in player's hand");
    }

    public void updateCheatAttempt(List<Card> trickCards, String playedColor) {
        String dutyCardColor = null;
        for (Card trickCard : trickCards) {
            if (trickCard.getCardType() != CardType.MISTLETOE
                    && trickCard.getCardType() != CardType.GOLDEN_SICKLE) {
                if (dutyCardColor == null)
                    dutyCardColor = trickCard.getColor();
            }
        }

        // Player played a color that does not match the color force (could be legal if no other choice)
        if (dutyCardColor != null && !dutyCardColor.equals(playedColor)) {
            for (Card handCard : cardsInHand) {
                if (handCard.getColor().equals(dutyCardColor)) {
                    cheatedInCurrentRound = true;
                    break;
                }
            }
        }
    }

    public boolean hasGaiaCard() {
        for (Card card : cardsInHand) {
            if (card.getCardType().equals(CardType.GAIA))
                return true;
        }
        return false;
    }

    public boolean addClaimedTrick(List<Card> trickCards) {
        Map<String, Card> lowestCardsInTrick = new HashMap<>();
        int goldenSickleCounter = 0;

        for (Card trickCard : trickCards) {
            String currentTrickCardColor = trickCard.getColor();
            int currentTrickCardValue = trickCard.getValue();

            if (trickCard.getCardType() == CardType.GOLDEN_SICKLE) {
                goldenSickleCounter++;
            } else {
                Card existingCard = lowestCardsInTrick.get(currentTrickCardColor);
                if (existingCard == null || currentTrickCardValue < existingCard.getValue()) {
                    lowestCardsInTrick.put(currentTrickCardColor, trickCard);
                }
            }
        }

        // update player trick
        for (Map.Entry<String, Card> entry : lowestCardsInTrick.entrySet()) {
            updateClaimedTrickForColor(entry.getKey(), entry.getValue());
        }

        // check for death
        boolean isPlayerDead = isPlayerDead();

        // check for GOLDEN_SICKLE and remove X colours
        if (!isPlayerDead && goldenSickleCounter > 0) {
            for (int i = 0; i < goldenSickleCounter; i++) {
                Card cardToReset = getHighestValueClaimedTrickCard();
                claimedTricks.removeIf(card -> card.getColor().equals(cardToReset.getColor()));
            }
        }

        return isPlayerDead;
    }

    public void updateClaimedTrickForColor(String color, Card newCard) {
        Iterator<Card> iterator = claimedTricks.iterator();
        while (iterator.hasNext()) {
            Card claimedCard = iterator.next();
            if (claimedCard.getColor().equals(color)) {
                iterator.remove();
                break;
            }
        }
        claimedTricks.add(newCard);
    }

    public boolean isPlayerDead() {
        return claimedTricks.stream().anyMatch(card -> card.getColor().equals(CardType.GREEN.getColor())) &&
                claimedTricks.stream().anyMatch(card -> card.getColor().equals(CardType.RED.getColor())) &&
                claimedTricks.stream().anyMatch(card -> card.getColor().equals(CardType.PURPLE.getColor())) &&
                claimedTricks.stream().anyMatch(card -> card.getColor().equals(CardType.BLUE.getColor())) &&
                claimedTricks.stream().anyMatch(card -> card.getColor().equals(CardType.YELLOW.getColor()));
    }

    public Card getHighestValueClaimedTrickCard() {
        Map<String, Card> maxValueCards = new HashMap<>();
        for (Card card : claimedTricks) {
            String color = card.getColor();
            if (!maxValueCards.containsKey(color) || card.getValue() > maxValueCards.get(color).getValue()) {
                maxValueCards.put(color, card);
            }
        }

        return maxValueCards.values().stream()
                .max(Comparator.comparingInt(Card::getValue))
                .orElse(null);
    }
}

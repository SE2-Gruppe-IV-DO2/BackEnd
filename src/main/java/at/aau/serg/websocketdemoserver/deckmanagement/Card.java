package at.aau.serg.websocketdemoserver.deckmanagement;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Comparator;

public class Card {
    CardType cardType;
    @Getter
    String color;
    @Getter
    Integer value;
    String imgPath;

    public Card(CardType cardType, Integer value){
        this.cardType = cardType;
        this.color = cardType.getColor();
        this.value = value;
        this.imgPath = this.color + this.value + ".png";
    }

    public Card(Card card) {
        this.cardType = card.cardType;
        this.color = card.color;
        this.value = card.value;
        this.imgPath = card.imgPath;
    }

    @Override
    public String toString() {
        return color + " " + value;
    }
}

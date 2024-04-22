package at.aau.serg.websocketdemoserver.deckmanagement;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Comparator;

@Data
public class Card {

    CardType cardType;
    String color;
    Integer value;
    String imgPath;

    public Card(CardType cardType, Integer value){
        this.cardType = cardType;
        this.color = cardType.getColor();
        this.value = value;
        this.imgPath = this.color + this.value + ".png";
    }

    @Override
    public String toString() {
        return color + " " + value;
    }
}

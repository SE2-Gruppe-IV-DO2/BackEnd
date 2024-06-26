package at.aau.serg.websocketdemoserver.deckmanagement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    CardType cardType;
    @Getter
    String color;
    @Getter
    Integer value;
    String imgPath;

    public Card() {
    }

    public Card(CardType cardType, String color, Integer value) {
        this.cardType = cardType;
        this.color = color;
        this.value = value;
        this.createImagePath();
    }

    public Card(CardType cardType, Integer value){
        this.cardType = cardType;
        this.color = cardType.getColor();
        this.value = value;
        this.createImagePath();
    }

    public Card(Card card) {
        this.cardType = card.cardType;
        this.color = card.color;
        this.value = card.value;
        this.imgPath = card.imgPath;
        this.createImagePath();
    }

    public Card(String color, Integer value) {
        this.cardType = CardType.getByColor(color);
        this.color = color;
        this.value = value;
        this.createImagePath();
    }

    @Override
    public String toString() {
        return color + " " + value;
    }


    public void createImagePath() {
        this.imgPath =  "card_" + cardType.getName() + this.value;
        if (this.cardType.getName().equals("gaia") && !this.color.isEmpty()) {
            this.imgPath += "_" + color;
        }
    }
}

package at.aau.serg.websocketdemoserver.deckmanagement;

public enum CardType {
    GAIA("gaia"),
    GOLDEN_SICKLE("golden_sickle"),
    MISTLETOE("mistletoe"),
    GREEN("green"),
    YELLOW("yellow"),
    RED("red"),
    BLUE("blue"),
    PURPLE("purple");

    private final String color;

    CardType(String color) {
        this.color = color;
    }

    String getColor(){
        return color;
    }


}

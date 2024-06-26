package at.aau.serg.websocketdemoserver.deckmanagement;

public enum CardType {
    GAIA("gaia", ""),
    GOLDEN_SICKLE("golden_sickle", ""),
    MISTLETOE("mistletoe", ""),
    GREEN("green", "green"),
    YELLOW("yellow", "yellow"),
    RED("red", "red"),
    BLUE("blue", "blue"),
    PURPLE("purple", "purple");

    private final String name;
    private final String color;

    CardType(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public static CardType getByName(String name) {
        for (CardType cardType : CardType.values()) {
            if (cardType.name().equalsIgnoreCase(name)) {
                return cardType;
            }
        }
        throw new IllegalArgumentException("No enum constant with name " + name);
    }

    public static CardType getByColor(String color) {
        for (CardType cardType : CardType.values()) {
            if (cardType.color.equalsIgnoreCase(color)) {
                return cardType;
            }
        }
        throw new IllegalArgumentException("No enum constant with color " + color);
    }

}

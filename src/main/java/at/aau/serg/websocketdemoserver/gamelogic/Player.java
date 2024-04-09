package at.aau.serg.websocketdemoserver.gamelogic;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {
    private final String playerID;
    @Setter
    private String playerName;

    public Player(String playerID, String playerName) {
        this.playerID = playerID;
        this.playerName = playerName;
    }
}

package at.aau.serg.websocketdemoserver.messaging.dtos;

import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CardPlayRequest {
    @JsonProperty("lobbyCode")
    private String lobbyCode;
    @JsonProperty("playerID")
    private String playerID;
    @JsonProperty("color")
    private String color;
    @JsonProperty("value")
    private Integer value;
}

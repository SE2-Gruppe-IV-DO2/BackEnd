package at.aau.serg.websocketdemoserver.messaging.dtos;

import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CardPlayedRequest {
    @JsonProperty("cardType")
    CardType cardType;
    @JsonProperty("color")
    String color;
    @JsonProperty("value")
    Integer value;
}

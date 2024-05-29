package at.aau.serg.websocketdemoserver.messaging.dtos;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HandCardsRequest {
    @JsonProperty("playerID")
    String playerID;
    @JsonProperty("handCards")
    List<Card> handCards;
}

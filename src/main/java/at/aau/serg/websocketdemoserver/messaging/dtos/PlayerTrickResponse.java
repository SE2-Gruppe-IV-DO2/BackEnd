package at.aau.serg.websocketdemoserver.messaging.dtos;

import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PlayerTrickResponse {
    @JsonProperty("playerTricks")
    HashMap<String, Map<CardType, Integer>> playerTricks;
}

package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ActivePlayerMessage {

    @JsonProperty("activePlayerId")
    private String activePlayerId;

    @JsonProperty("activePlayerName")
    private String activePlayerName;
}

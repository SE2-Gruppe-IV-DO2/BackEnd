package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TrickWonMessage {
    @JsonProperty("winningPlayerId")
    private String winningPlayerId;

    @JsonProperty("winningPlayerName")
    private String winningPlayerName;
}
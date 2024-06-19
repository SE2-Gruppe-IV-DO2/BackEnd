package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CheatAccusationRequest {

    @JsonProperty("userID")
    private String userID;

    @JsonProperty("lobbyCode")
    String lobbyCode;

    @JsonProperty("accusedUserId")
    private String accusedUserId;

    @JsonProperty("correctAccusation")
    private boolean correctAccusation;
}

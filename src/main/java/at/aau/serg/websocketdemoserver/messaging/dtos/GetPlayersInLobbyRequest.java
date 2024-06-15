package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetPlayersInLobbyRequest {
    @JsonProperty("lobbyCode")
    private String lobbyCode;
}

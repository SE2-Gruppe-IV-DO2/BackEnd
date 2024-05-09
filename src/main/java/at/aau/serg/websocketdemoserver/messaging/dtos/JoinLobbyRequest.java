package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JoinLobbyRequest {
    @JsonProperty("lobbyCode")
    private String lobbyCode;

    @JsonProperty("userID")
    private String userID;

    @JsonProperty("userName")
    private String userName;
}

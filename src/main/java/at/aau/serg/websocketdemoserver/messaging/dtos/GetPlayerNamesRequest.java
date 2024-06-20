package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetPlayerNamesRequest {
    @JsonProperty("lobbyCode")
    String lobbyCode;
}

package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class LobbyCreationRequest {
    @JsonProperty("userID")
    private String userID;

    @JsonProperty("userName")
    private String userName;
}
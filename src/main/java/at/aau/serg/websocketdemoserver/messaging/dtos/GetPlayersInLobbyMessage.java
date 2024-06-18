package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetPlayersInLobbyMessage {
    @JsonProperty("lobbyCode")
    private String lobbyCode;

    @JsonProperty("playerNames")
    private List<String> playerNames;

    @JsonProperty("playerNamesAndIds")
    private Map<String, String> playerNamesAndIds;
}

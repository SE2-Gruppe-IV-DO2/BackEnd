package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PlayerNamesResponse {
    @JsonProperty("playerNames")
    List<String> playerNames;
}

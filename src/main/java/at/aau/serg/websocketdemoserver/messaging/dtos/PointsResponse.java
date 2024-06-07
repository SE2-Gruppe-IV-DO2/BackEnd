package at.aau.serg.websocketdemoserver.messaging.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PointsResponse {
    @JsonProperty("pointsMap")
    Map<String, HashMap<Integer, Integer>> playerPoints;
}

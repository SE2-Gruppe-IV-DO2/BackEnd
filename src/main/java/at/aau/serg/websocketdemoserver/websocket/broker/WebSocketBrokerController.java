package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import at.aau.serg.websocketdemoserver.messaging.dtos.JoinLobbyRequest;
import at.aau.serg.websocketdemoserver.messaging.dtos.LobbyCreationRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketBrokerController {
    private LobbyManager lobbyManager = LobbyManager.getInstance();

    @MessageMapping("/hello")
    @SendTo("/topic/hello-response")
    public String handleHello(String message) {
        // TODO handle the messages here
        return "echo from broker: " + HtmlUtils.htmlEscape(message);
    }

    @MessageMapping("/create_new_lobby")
    @SendTo("/topic/lobby-created")
    public String createNewLobby(LobbyCreationRequest creationRequest) throws Exception {
        // Create a new lobby and return its ID
        String newlyCreatedLobbyCode = lobbyManager.createLobby();
        lobbyManager.addPlayerToLobby(newlyCreatedLobbyCode, creationRequest.getUserID(), creationRequest.getUserName());
        return newlyCreatedLobbyCode;
    }

    @MessageMapping("/join_lobby")
    @SendTo("/topic/lobby-joined")
    public String joinLobby(JoinLobbyRequest joinLobbyRequest) {
        // Join an existing lobby
        lobbyManager.addPlayerToLobby(joinLobbyRequest.getLobbyCode(), joinLobbyRequest.getUserID(), joinLobbyRequest.getUserName());

        return joinLobbyRequest.getLobbyCode();
    }

    @MessageMapping("/deal_new_round")
    @SendTo("/topic/new-round-dealt")
    public String dealNewRound(String lobbyCode) throws Exception{
        lobbyManager.dealNewRound(lobbyCode);
        return "";
    }

    @MessageMapping("/start_game_for_lobby")
    @SendTo("/topic/game_for_lobby_started")
    public String startGameForLobby(String lobbyCode) {
        lobbyManager.startGameForLobby(lobbyCode);

        return "Game started!";
    }

    //@MessageMapping("/TEST_PLAY_CARD")
    //@SendTo("/topic/active_player_changed")
    //public String messageForChangeOfActivePlayer(String lobbyCode) throws Exception {
    //    lobbyManager.endCurrentPlayersTurnForLobby(lobbyCode);
    //    return lobbyManager.getActivePlayerForLobby(lobbyCode);
    //}
}

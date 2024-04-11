package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketBrokerController {

    @MessageMapping("/hello")
    @SendTo("/topic/hello-response")
    public String handleHello(String message) {
        return "echo from broker: " + HtmlUtils.htmlEscape(message);
    }

    @MessageMapping("/create_new_lobby")
    @SendTo("/topic/lobby-created")
    public TextMessage createNewLobby(String userID) throws Exception {
        // Create a new lobby and return its ID
        String userName = "TEST";
        LobbyManager lobbyManager = LobbyManager.getInstance();
        String newlyCreatedLobbyCode = lobbyManager.createLobby();
        lobbyManager.addPlayerToLobby(newlyCreatedLobbyCode, userID, userName);
        return new TextMessage(newlyCreatedLobbyCode);
    }

    @MessageMapping("/join_lobby")
    @SendTo("/topic/lobby-joined")
    public String joinLobby(String lobbyCode, String userID, String userName) {
        // Join an existing lobby
        LobbyManager lobbyManager = LobbyManager.getInstance();
        lobbyManager.addPlayerToLobby(lobbyCode, userID, userName);

        return "";
    }

}

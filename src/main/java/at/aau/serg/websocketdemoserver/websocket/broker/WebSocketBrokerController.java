package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketBrokerController {

    @MessageMapping("/hello")
    @SendTo("/topic/hello-response")
    public String handleHello(String message) {
        // TODO handle the messages here
        return "echo from broker: " + HtmlUtils.htmlEscape(message);
    }

    @MessageMapping("/create_new_lobby")
    @SendTo("/topic/lobby-created")
    public String createNewLobby() throws Exception {
        // Create a new lobby and return its ID
        LobbyManager lobbyManager = LobbyManager.getInstance();
        return lobbyManager.createLobby();
    }

}

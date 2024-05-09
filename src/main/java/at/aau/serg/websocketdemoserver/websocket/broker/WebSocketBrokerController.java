package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import at.aau.serg.websocketdemoserver.gamelogic.Lobby;
import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import at.aau.serg.websocketdemoserver.gamelogic.Player;
import at.aau.serg.websocketdemoserver.messaging.dtos.CardPlayRequest;
import at.aau.serg.websocketdemoserver.messaging.dtos.JoinLobbyRequest;
import at.aau.serg.websocketdemoserver.messaging.dtos.LobbyCreationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WebSocketBrokerController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

    @MessageMapping("/play_card")
    @SendTo("/topic/card_played")
    public CardPlayRequest playCard(CardPlayRequest playCardRequest) throws Exception {
        lobbyManager.cardPlayed(playCardRequest);
        endTurnForActivePlayer(playCardRequest.getLobbyCode());
        return playCardRequest;
    }
    private void endTurnForActivePlayer(String lobbyCode) throws Exception {
        lobbyManager.endCurrentPlayersTurnForLobby(lobbyCode);
        String activePlayerId = lobbyManager.getActivePlayerForLobby(lobbyCode);
        messagingTemplate.convertAndSend("/topic/active_player_changed", activePlayerId);
    }
}

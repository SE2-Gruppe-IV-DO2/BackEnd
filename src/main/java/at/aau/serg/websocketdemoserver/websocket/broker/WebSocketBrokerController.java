package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import at.aau.serg.websocketdemoserver.messaging.dtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketBrokerController {
    private LobbyManager lobbyManager = LobbyManager.getInstance();
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        sendPlayerJoinedLobbyMessage(joinLobbyRequest.getUserName());
        return joinLobbyRequest.getLobbyCode();
    }

    @MessageMapping("/deal_new_round")
    @SendTo("/topic/new-round-dealt")
    public String dealNewRound(DealRoundRequest dealRoundRequest) throws Exception{
        lobbyManager.dealNewRound(dealRoundRequest.getLobbyCode());
        HandCardsRequest handCardsRequest = new HandCardsRequest();
        handCardsRequest.setPlayerID(dealRoundRequest.getUserID());
        handCardsRequest.setHandCards(lobbyManager.getLobbyByCode(dealRoundRequest.getLobbyCode()).getPlayerByID(dealRoundRequest.getUserID()).getCardsInHand());

        lobbyManager.setGaiaPlayerAsStartPlayer(dealRoundRequest.getLobbyCode());
        sendActivePlayerMessage(dealRoundRequest.getLobbyCode());

        return objectMapper.writeValueAsString(handCardsRequest);
    }

    @MessageMapping("/start_game_for_lobby")
    @SendTo("/topic/game_for_lobby_started")
    public String startGameForLobby(StartGameRequest startGameRequest) {
        lobbyManager.startGameForLobby(startGameRequest.getLobbyCode());

        return "Game started!";
    }

    @MessageMapping("/play_card")
    public void playCard(CardPlayRequest playCardRequest) throws Exception {
        Card card  = lobbyManager.cardPlayed(playCardRequest);
        CardPlayedRequest cardPlayedRequest = new CardPlayedRequest();
        cardPlayedRequest.setCardType(card.getCardType());
        cardPlayedRequest.setColor(card.getColor());
        cardPlayedRequest.setValue(String.valueOf(card.getValue()));
        endTurnForActivePlayer(playCardRequest.getLobbyCode());

        messagingTemplate.convertAndSend("/topic/card_played", cardPlayedRequest);
    }
    private void endTurnForActivePlayer(String lobbyCode) throws Exception {
        lobbyManager.endCurrentPlayersTurnForLobby(lobbyCode);
        sendActivePlayerMessage(lobbyCode);
    }

    private void sendActivePlayerMessage(String lobbyCode) throws Exception {
        String activePlayerId = lobbyManager.getActivePlayerForLobby(lobbyCode);
        messagingTemplate.convertAndSend("/topic/active_player_changed", activePlayerId);
    }

    private void sendPlayerJoinedLobbyMessage(String playerName) {
        messagingTemplate.convertAndSend("/topic/player_joined_lobby", playerName);
    }
}

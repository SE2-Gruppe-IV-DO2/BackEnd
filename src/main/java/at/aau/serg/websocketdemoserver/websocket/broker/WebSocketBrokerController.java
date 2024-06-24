package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.gamelogic.Lobby;
import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import at.aau.serg.websocketdemoserver.gamelogic.Player;
import at.aau.serg.websocketdemoserver.messaging.dtos.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

@Controller
public class WebSocketBrokerController {
    private final LobbyManager lobbyManager = LobbyManager.getInstance();
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
    public void createNewLobby(LobbyCreationRequest creationRequest) throws Exception {
        String newlyCreatedLobbyCode = lobbyManager.createLobby();
        lobbyManager.addPlayerToLobby(newlyCreatedLobbyCode, creationRequest.getUserID(), creationRequest.getUserName());
        messagingTemplate.convertAndSend("/topic/lobby-created/" + creationRequest.getUserID(), newlyCreatedLobbyCode);
    }

    @MessageMapping("/join_lobby")
    public void joinLobby(JoinLobbyRequest joinLobbyRequest) {
        lobbyManager.addPlayerToLobby(joinLobbyRequest.getLobbyCode(), joinLobbyRequest.getUserID(), joinLobbyRequest.getUserName());
        sendPlayerJoinedLobbyMessage(joinLobbyRequest.getUserName(), joinLobbyRequest.getLobbyCode());

        JoinLobbyResponse joinLobbyResponse = new JoinLobbyResponse();
        joinLobbyResponse.setLobbyCode(joinLobbyRequest.getLobbyCode());

        try {
            messagingTemplate.convertAndSend("/topic/lobby-joined/" + joinLobbyRequest.getLobbyCode() + "/" + joinLobbyRequest.getUserID(), objectMapper.writeValueAsString(joinLobbyResponse));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @MessageMapping("/get_players_in_lobby")
    public void getPlayersInLobby(GetPlayersInLobbyRequest playersInLobbyRequest) throws Exception {
        List<String> playerNames = lobbyManager.getPlayerNamesForLobby(playersInLobbyRequest.getLobbyCode());
        GetPlayersInLobbyMessage playersInLobbyMessage = new GetPlayersInLobbyMessage();
        playersInLobbyMessage.setLobbyCode(playersInLobbyRequest.getLobbyCode());
        playersInLobbyMessage.setPlayerNames(playerNames);

        Map<String, String> playerNamesAndIds = lobbyManager.getPlayerNamesWithIdsForLobby(playersInLobbyRequest.getLobbyCode());
        playersInLobbyMessage.setPlayerNamesAndIds(playerNamesAndIds);

        messagingTemplate.convertAndSend("/topic/players_in_lobby/" + playersInLobbyRequest.getLobbyCode(), objectMapper.writeValueAsString(playersInLobbyMessage));
    }

    @MessageMapping("/deal_new_round")
    @SendTo("/topic/new-round-dealt")
    public void dealNewRound(DealRoundRequest dealRoundRequest) throws Exception{
        lobbyManager.dealNewRound(dealRoundRequest.getLobbyCode());
        HandCardsRequest handCardsRequest = new HandCardsRequest();
        handCardsRequest.setPlayerID(dealRoundRequest.getUserID());
        handCardsRequest.setHandCards(lobbyManager.getLobbyByCode(dealRoundRequest.getLobbyCode()).getPlayerByID(dealRoundRequest.getUserID()).getCardsInHand());

        lobbyManager.setGaiaPlayerAsStartPlayer(dealRoundRequest.getLobbyCode());
        sendActivePlayerMessage(dealRoundRequest.getLobbyCode());

        messagingTemplate.convertAndSend("/topic/new-round-dealt/" + dealRoundRequest.getLobbyCode() + "/" + dealRoundRequest.getUserID(), objectMapper.writeValueAsString(handCardsRequest));
    }

    @MessageMapping("/start_game_for_lobby")
    public void startGameForLobby(StartGameRequest startGameRequest) {
        lobbyManager.startGameForLobby(startGameRequest.getLobbyCode());

        StartGameResponse startGameResponse = new StartGameResponse();
        startGameResponse.setResponse("Game started!");

        try {
            messagingTemplate.convertAndSend("/topic/game_for_lobby_started/" + startGameRequest.getLobbyCode(), objectMapper.writeValueAsString(startGameResponse));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @MessageMapping("/play_card")
    public void playCard(CardPlayRequest playCardRequest) throws Exception {
        Card card  = lobbyManager.cardPlayed(playCardRequest);
        CardPlayedRequest cardPlayedRequest = new CardPlayedRequest();
        cardPlayedRequest.setCardType(card.getCardType());
        cardPlayedRequest.setColor(card.getColor());
        cardPlayedRequest.setValue(String.valueOf(card.getValue()));

        messagingTemplate.convertAndSend("/topic/card_played/" + playCardRequest.getLobbyCode(), cardPlayedRequest);

        Lobby currentLobby = lobbyManager.getLobbyByID(playCardRequest.getLobbyCode());
        if (currentLobby.isCurrentTrickDone()) {
            Player winningPlayer = currentLobby.evaluateAndHandoutTrick();
            sendPlayerWonTrickMessage(winningPlayer.getPlayerID(), winningPlayer.getPlayerName(), currentLobby.getLobbyCode());

            sendActivePlayerMessage(playCardRequest.getLobbyCode());

            if (currentLobby.isRoundFinished()) {
                currentLobby.endRound();
                endRoundForLobby(playCardRequest.getLobbyCode());
            }
        }
        else {
            endTurnForActivePlayer(playCardRequest.getLobbyCode());
        }
    }

    @MessageMapping("/accuse_player_of_cheating")
    public void accusePlayerOfCheating(CheatAccusationRequest cheatAccusationRequest) throws Exception {
        Lobby currentLobby = lobbyManager.getLobbyByCode(cheatAccusationRequest.getLobbyCode());

        Player player = currentLobby.getPlayerByID(cheatAccusationRequest.getUserID());
        currentLobby.setNumberOfCheatAccusations(currentLobby.getNumberOfCheatAccusations() + 1);

        if (cheatAccusationRequest.getAccusedUserId().isEmpty()) {
            messagingTemplate.convertAndSend("/topic/accusation_result/" + player.getPlayerID(), objectMapper.writeValueAsString(cheatAccusationRequest));
        }
        else {
            Player accusedPlayer = currentLobby.getPlayerByID(cheatAccusationRequest.getAccusedUserId());

            currentLobby.adjustPointsAfterCheatingAccusation(player, accusedPlayer.isCheatedInCurrentRound());
            currentLobby.adjustPointsAfterCheatingAccusation(accusedPlayer, !accusedPlayer.isCheatedInCurrentRound());

            cheatAccusationRequest.setCorrectAccusation(accusedPlayer.isCheatedInCurrentRound());

            messagingTemplate.convertAndSend("/topic/accusation_result/" + player.getPlayerID(), objectMapper.writeValueAsString(cheatAccusationRequest));
        }

        if (currentLobby.getNumberOfCheatAccusations() == currentLobby.getPlayers().stream().count()) {
            currentLobby.resetCheatAttempts();
        }
    }

    @MessageMapping("/get_points")
    public void getPoints(PointsRequest pointsRequest) {
        String lobbyCode = pointsRequest.getLobbyCode();
        Lobby targetLobby = lobbyManager.getLobbyByID(lobbyCode);

        PointsResponse pointsResponse = new PointsResponse();
        pointsResponse.setPlayerPoints(targetLobby.getPlayerPoints());

        try {
            messagingTemplate.convertAndSend("/topic/points/" + lobbyCode, objectMapper.writeValueAsString(pointsResponse));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @MessageMapping("/get-player-names")
    public void getPlayerNames(GetPlayerNamesRequest getPlayerNamesRequest) {
        Lobby targetLobby = lobbyManager.getLobbyByID(getPlayerNamesRequest.getLobbyCode());
        PlayerNamesResponse playerNamesResponse = new PlayerNamesResponse();
        playerNamesResponse.setPlayerNames(targetLobby.getPlayerNames());

        try {
            messagingTemplate.convertAndSend("/topic/player_names/" + getPlayerNamesRequest.getLobbyCode(), objectMapper.writeValueAsString(playerNamesResponse));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @MessageMapping("/get-player-tricks")
    public void sendPlayerTricks(String lobbyCode) {
        PlayerTrickResponse playerTrickResponse = new PlayerTrickResponse();
        Lobby targetLobby = lobbyManager.getLobbyByID(lobbyCode);
        playerTrickResponse.setPlayerTricks(targetLobby.getPlayerTricks());

        System.out.println(playerTrickResponse.getPlayerTricks().toString());

        try {
            messagingTemplate.convertAndSend("/topic/player_tricks/" + lobbyCode, objectMapper.writeValueAsString(playerTrickResponse));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void endRoundForLobby(String lobbyCode) {
        messagingTemplate.convertAndSend("/topic/round_ended/" + lobbyCode, "Round ended");
    }

    private void endTurnForActivePlayer(String lobbyCode) throws Exception {
        lobbyManager.endCurrentPlayersTurnForLobby(lobbyCode);
        sendActivePlayerMessage(lobbyCode);
    }

    private void sendActivePlayerMessage(String lobbyCode) throws Exception {
        Player activePlayer = lobbyManager.getActivePlayerForLobby(lobbyCode);
        ActivePlayerMessage message = new ActivePlayerMessage();
        message.setActivePlayerId(activePlayer.getPlayerID());
        message.setActivePlayerName(activePlayer.getPlayerName());
        messagingTemplate.convertAndSend("/topic/active_player_changed/" + lobbyCode, message);
    }

    private void sendPlayerJoinedLobbyMessage(String playerName, String lobbyCode) {
        messagingTemplate.convertAndSend("/topic/player_joined_lobby/" + lobbyCode, playerName);
    }

    private void sendPlayerWonTrickMessage(String playerId, String playerName, String lobbyCode) {
        TrickWonMessage trickMessage = new TrickWonMessage();
        trickMessage.setWinningPlayerId(playerId);
        trickMessage.setWinningPlayerName(playerName);
        messagingTemplate.convertAndSend("/topic/trick_won/" + lobbyCode, trickMessage);
    }
}

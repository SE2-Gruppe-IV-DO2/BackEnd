package at.aau.serg.websocketdemoserver;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import at.aau.serg.websocketdemoserver.messaging.dtos.HandCardsRequest;
import at.aau.serg.websocketdemoserver.messaging.dtos.JoinLobbyResponse;
import at.aau.serg.websocketdemoserver.messaging.dtos.PointsRequest;
import at.aau.serg.websocketdemoserver.messaging.dtos.PointsResponse;
import at.aau.serg.websocketdemoserver.websocket.StompFrameHandlerClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketBrokerIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String WEBSOCKET_URI = "ws://localhost:%d/websocket-example-broker";
    private final String WEBSOCKET_TOPIC_HELLO_RESPONSE = "/topic/hello-response";

    private final String WEBSOCKET_TOPIC_CREATE_LOBBY = "/app/create_new_lobby";
    private final String WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE = "/topic/lobby-created";

    private final String WEBSOCKET_TOPIC_GET_PLAYERS_IN_LOBBY = "/app/get_players_in_lobby";
    private final String WEBSOCKET_TOPIC_GET_PLAYERS_IN_LOBBY_RESPONSE = "/topic/players_in_lobby";

    private final String WEBSOCKET_TOPIC_JOIN_LOBBY = "/app/join_lobby";
    private final String WEBSOCKET_TOPIC_JOIN_LOBBY_RESPONSE = "/topic/lobby-joined";
    private final String WEBSOCKET_TOPIC_PLAYER_JOINED_LOBBY_RESPONSE = "/topic/player_joined_lobby";

    private final String WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY = "/app/start_game_for_lobby";
    private final String WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY_RESPONSE = "/topic/game_for_lobby_started";

    private final String WEBSOCKET_TOPIC_DEAL_NEW_ROUND = "/app/deal_new_round";
    private final String WEBSOCKET_TOPIC_DEAL_NEW_ROUND_RESPONSE = "/topic/new-round-dealt";

    private final String WEBSOCKET_TOPIC_PLAY_CARD = "/app/play_card";
    private final String WEBSOCKET_TOPIC_CARD_PLAYED_RESPONSE = "/topic/card_played";

    private final String WEBSOCKET_TOPIC_ACTIVE_PLAYER_CHANGED_RESPONSE = "/topic/active_player_changed";
    private final String WEBSOCKET_TOPIC_PLAYER_HAS_WON_TRICK = "/topic/trick_won";

    private final String WEBSOCKET_TOPIC_GET_POINTS = "/app/get_points";
    private final String WEBSOCKET_TOPIC_GET_POINTS_RESPONSE = "/topic/points/";


    /**
     * Queue of messages from the server.
     */
    BlockingQueue<String> messages = new LinkedBlockingDeque<>();

    @Test
    public void testWebSocketMessageBroker() throws Exception {
        StompSession session = initStompSession(WEBSOCKET_TOPIC_HELLO_RESPONSE);

        // send a message to the server
        String message = "Test message";
        session.send("/app/hello", message);

        var expectedResponse = "echo from broker: " + message;
        assertThat(messages.poll(1, TimeUnit.SECONDS)).isEqualTo(expectedResponse);
    }

    @Test
    public void testWebSocketCreateNewLobby() throws Exception {
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE + "/TEST_USER_ID");

        // send a message to the server
        //Player player = new Player("TEST_USER", "TEST_USER_NAME");
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";

        JSONObject payload = new JSONObject();
        payload.put("userID", userID);
        payload.put("userName", userName);

        session.send(WEBSOCKET_TOPIC_CREATE_LOBBY, payload);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(createLobbyResponse).isNotEmpty();
        assertEquals(createLobbyResponse.length(), LobbyManager.LOBBY_CODE_LENGTH);
    }

    @Test
    public void testWebSocketCreateNewLobbyWithoutUser() throws Exception {
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE + "/");

        // send a message to the server
        JSONObject payload = new JSONObject();
        payload.put("userID", "");
        payload.put("userName", "");
        session.send(WEBSOCKET_TOPIC_CREATE_LOBBY, payload);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(createLobbyResponse).isNullOrEmpty();
    }

    @Test
    public void testWebSocketJoinLobby() throws Exception {
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE + "/TEST_USER_ID");

        // send a message to the server
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";

        JSONObject payload = new JSONObject();
        payload.put("userID", userID);
        payload.put("userName", userName);

        session.send(WEBSOCKET_TOPIC_CREATE_LOBBY, payload);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(createLobbyResponse).isNotEmpty();
        assertEquals(createLobbyResponse.length(), LobbyManager.LOBBY_CODE_LENGTH);

        StompSession sessionJoin = initStompSession(WEBSOCKET_TOPIC_JOIN_LOBBY_RESPONSE + "/" + createLobbyResponse + "/TEST_USER_ID_2");

        String userIDJoin = "TEST_USER_ID_2";
        String userNameJoin = "TEST_USER_NAME_2";

        JSONObject joinLobbyPayload = new JSONObject();
        joinLobbyPayload.put("lobbyCode", createLobbyResponse);
        joinLobbyPayload.put("userID", userIDJoin);
        joinLobbyPayload.put("userName", userNameJoin);

        sessionJoin.send(WEBSOCKET_TOPIC_JOIN_LOBBY, joinLobbyPayload);

        JoinLobbyResponse joinLobbyResponse1 = new JoinLobbyResponse();
        joinLobbyResponse1.setLobbyCode(createLobbyResponse);

        String joinLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(joinLobbyResponse).isNotEmpty();
        JoinLobbyResponse actual = objectMapper.readValue(joinLobbyResponse, JoinLobbyResponse.class);
        assertEquals(joinLobbyResponse1, actual);
    }

    @Test
    public void testPlayerJoinedLobby() throws Exception {
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE + "/TEST_USER_ID");

        // send a message to the server
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";

        JSONObject payload = new JSONObject();
        payload.put("userID", userID);
        payload.put("userName", userName);

        session.send(WEBSOCKET_TOPIC_CREATE_LOBBY, payload);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(createLobbyResponse).isNotEmpty();
        assertEquals(createLobbyResponse.length(), LobbyManager.LOBBY_CODE_LENGTH);

        StompSession sessionJoin = initStompSession(WEBSOCKET_TOPIC_JOIN_LOBBY_RESPONSE + "/" + createLobbyResponse + "/TEST_USER_ID_2");
        initStompSession(WEBSOCKET_TOPIC_PLAYER_JOINED_LOBBY_RESPONSE + "/" + createLobbyResponse);

        String userIDJoin = "TEST_USER_ID_2";
        String userNameJoin = "TEST_USER_NAME_2";

        JSONObject joinLobbyPayload = new JSONObject();
        joinLobbyPayload.put("lobbyCode", createLobbyResponse);
        joinLobbyPayload.put("userID", userIDJoin);
        joinLobbyPayload.put("userName", userNameJoin);

        sessionJoin.send(WEBSOCKET_TOPIC_JOIN_LOBBY, joinLobbyPayload);

        String firstResponse = messages.poll(1, TimeUnit.SECONDS);
        String secondResponse = messages.poll(1, TimeUnit.SECONDS);
        String joinLobbyResponse = "";
        String playerJoinedResponse = "";

        if (firstResponse.length() == LobbyManager.LOBBY_CODE_LENGTH) {
            joinLobbyResponse = firstResponse;
            playerJoinedResponse = secondResponse;
        }
        else {
            joinLobbyResponse = secondResponse;
            playerJoinedResponse = firstResponse;
        }

        JoinLobbyResponse actualJoinResponse = objectMapper.readValue(joinLobbyResponse, JoinLobbyResponse.class);

        JoinLobbyResponse expectedJoinResponse = new JoinLobbyResponse();
        expectedJoinResponse.setLobbyCode(createLobbyResponse);

        assertThat(joinLobbyResponse).isNotEmpty();
        assertEquals(expectedJoinResponse, actualJoinResponse);

        assertEquals(playerJoinedResponse, userNameJoin);
    }

    @Test
    public void testGetPlayersInLobby() throws Exception {
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE + "/TEST_USER_ID");

        // send a message to the server
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";

        JSONObject payload = new JSONObject();
        payload.put("userID", userID);
        payload.put("userName", userName);

        session.send(WEBSOCKET_TOPIC_CREATE_LOBBY, payload);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(createLobbyResponse).isNotEmpty();
        assertEquals(createLobbyResponse.length(), LobbyManager.LOBBY_CODE_LENGTH);

        StompSession sessionPlayersInLobby = initStompSession(WEBSOCKET_TOPIC_GET_PLAYERS_IN_LOBBY_RESPONSE + "/" + createLobbyResponse);
        JSONObject getLobbyPlayersRequest = new JSONObject();
        getLobbyPlayersRequest.put("lobbyCode", createLobbyResponse);
        sessionPlayersInLobby.send(WEBSOCKET_TOPIC_GET_PLAYERS_IN_LOBBY, getLobbyPlayersRequest);

        String playersResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(playersResponse).isNotEmpty();
    }


    @Test
    public void testWebSocketDealNewRound() throws Exception {
        String lobbyCode = setUpLobby();
        setUpTwoPlayerJoinLobby(lobbyCode);
        setUpStartGame(lobbyCode);

        JSONObject payload = new JSONObject();
        payload.put("lobbyCode", lobbyCode);
        payload.put("userID", "TEST_USER_ID");
        StompSession dealNewRoundSession = initStompSession(WEBSOCKET_TOPIC_DEAL_NEW_ROUND_RESPONSE + "/" + lobbyCode + "/TEST_USER_ID");
        dealNewRoundSession.send(WEBSOCKET_TOPIC_DEAL_NEW_ROUND, payload);
        String dealNewRoundResponse = messages.poll(1, TimeUnit.SECONDS);
        System.out.println("dealNewRoundResponse:" + dealNewRoundResponse);
        assert dealNewRoundResponse != null;
    }

    @Test
    public void testWebSocketStartGameForLobby() throws Exception {

        // create a new lobby and start the game
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userID", userID);
        jsonObject.put("userName", userName);

        StompSession lobbyCreationSession = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE + "/TEST_USER_ID" );
        lobbyCreationSession.send(WEBSOCKET_TOPIC_CREATE_LOBBY, jsonObject);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        System.out.println("createLobbyResponse:" + createLobbyResponse);

        assert createLobbyResponse != null;
        StompSession startGameSession = initStompSession(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY_RESPONSE + "/" + createLobbyResponse);

        JSONObject startLobbyRequest = new JSONObject();
        startLobbyRequest.put("lobbyCode", createLobbyResponse);
        startGameSession.send(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY, startLobbyRequest);
        String startGameResponse = messages.poll(1, TimeUnit.SECONDS);

        assertThat(startGameResponse).isNotEmpty();
    }

    @Test
    void testPlayCardEndpoint() throws Exception {
        String lobbyCode = setUpLobby();
        setUpTwoPlayerJoinLobby(lobbyCode);
        setUpStartGame(lobbyCode);
        List<Card> cardList = setUpDealNewRound(lobbyCode);
        Card card = cardList.get(0);

        JSONObject payload = new JSONObject();
        payload.put("lobbyCode", lobbyCode);
        payload.put("userID", "TEST_USER_ID");
        payload.put("color", card.getColor());
        payload.put("value", String.valueOf(card.getValue()));

        StompSession playCardSession = initStompSession(WEBSOCKET_TOPIC_CARD_PLAYED_RESPONSE + "/" + lobbyCode);
        playCardSession.send(WEBSOCKET_TOPIC_PLAY_CARD, payload);
        String playCardResponse = messages.poll(1, TimeUnit.SECONDS);
        Assertions.assertNull(playCardResponse);
    }

    @Test
    void testActivePlayerChangedEndpoint() throws Exception {
        String lobbyCode = setUpLobby();
        setUpTwoPlayerJoinLobby(lobbyCode);
        setUpStartGame(lobbyCode);

        List<Card> cardList = setUpDealNewRound(lobbyCode);
        Card card = cardList.get(0);

        JSONObject payload = new JSONObject();
        payload.put("lobbyCode", lobbyCode);
        payload.put("userID", "TEST_USER_ID");
        payload.put("color", card.getColor());
        payload.put("value", card.getValue());

        StompSession playCardSession = initStompSession(WEBSOCKET_TOPIC_CARD_PLAYED_RESPONSE + "/" + lobbyCode);
        initStompSession(WEBSOCKET_TOPIC_ACTIVE_PLAYER_CHANGED_RESPONSE);
        playCardSession.send(WEBSOCKET_TOPIC_PLAY_CARD, payload);
        messages.poll(1, TimeUnit.SECONDS);
        String playerChangedResponse = messages.poll(1, TimeUnit.SECONDS);
        Assertions.assertNull(playerChangedResponse);
    }

    @Test
    void testPlayerHasWonTrickEndpoint() throws Exception {
        String lobbyCode = setUpLobby();
        setUpTwoPlayerJoinLobby(lobbyCode);
        setUpStartGame(lobbyCode);

        List<Card> cardList = setUpDealNewRound(lobbyCode);
        Card card = cardList.get(0);

        JSONObject payload = new JSONObject();
        payload.put("lobbyCode", lobbyCode);
        payload.put("userID", "TEST_USER_ID");
        payload.put("color", card.getColor());
        payload.put("value", card.getValue());

        StompSession playCardSession = initStompSession(WEBSOCKET_TOPIC_CARD_PLAYED_RESPONSE + "/" + lobbyCode);
        initStompSession(WEBSOCKET_TOPIC_ACTIVE_PLAYER_CHANGED_RESPONSE + "/" + lobbyCode);
        initStompSession(WEBSOCKET_TOPIC_PLAYER_HAS_WON_TRICK);
        playCardSession.send(WEBSOCKET_TOPIC_PLAY_CARD, payload);
        messages.poll(1, TimeUnit.SECONDS);

        String playerChangedResponse = messages.poll(1, TimeUnit.SECONDS);
        Assertions.assertNull(playerChangedResponse);

        payload = new JSONObject();
        payload.put("lobbyCode", lobbyCode);
        payload.put("userID", "TEST_USER_ID");
        card = cardList.get(1);
        payload.put("color", card.getColor());
        payload.put("value", card.getValue());

        playCardSession.send(WEBSOCKET_TOPIC_PLAY_CARD, payload);

        String playerHasWonTrickMessage = messages.poll(1, TimeUnit.SECONDS);
        Assertions.assertNull(playerHasWonTrickMessage);

        playerChangedResponse = messages.poll(1, TimeUnit.SECONDS);
        Assertions.assertNull(playerChangedResponse);

        payload = new JSONObject();
        payload.put("lobbyCode", lobbyCode);
        payload.put("userID", "TEST_USER_ID");
        card = cardList.get(2);
        payload.put("color", card.getColor());
        payload.put("value", card.getValue());

        playCardSession.send(WEBSOCKET_TOPIC_PLAY_CARD, payload);

        playerHasWonTrickMessage = messages.poll(1, TimeUnit.SECONDS);
        Assertions.assertNull(playerHasWonTrickMessage);

        playerChangedResponse = messages.poll(1, TimeUnit.SECONDS);
        Assertions.assertNull(playerChangedResponse);
    }

    @Test
    void testGetPointsEndpoint() throws Exception {
        String lobbyCode = setUpLobby();
        setUpTwoPlayerJoinLobby(lobbyCode);
        setUpStartGame(lobbyCode);

        PointsRequest pointsRequest = new PointsRequest();
        pointsRequest.setLobbyCode(lobbyCode);

        StompSession getPointsSession = initStompSession(WEBSOCKET_TOPIC_GET_POINTS_RESPONSE + lobbyCode);
        getPointsSession.send(WEBSOCKET_TOPIC_GET_POINTS, pointsRequest);

        String response = messages.poll(5, TimeUnit.SECONDS);
        System.out.println("Received response: " + response);
        Assertions.assertNotNull(response);

        PointsResponse pointsResponse = new ObjectMapper().readValue(response, PointsResponse.class);
        Map<String, Map<Integer, Integer>> expected = new HashMap<>();
        expected.put("TEST_USER_NAME", new HashMap<>());
        expected.get("TEST_USER_NAME").put(-1, 0);
        expected.put("TEST_USER_NAME_2", new HashMap<>());
        expected.get("TEST_USER_NAME_2").put(-1, 0);
        expected.put("TEST_USER_NAME_3", new HashMap<>());
        expected.get("TEST_USER_NAME_3").put(-1, 0);

        Assertions.assertEquals(expected, pointsResponse.getPlayerPoints());
    }

    /**
     * @return The Stomp session for the WebSocket connection (Stomp - WebSocket is comparable to HTTP - TCP).
     */
    public StompSession initStompSession(String websocketTopic) throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        List<MessageConverter> converters = new ArrayList<>();
        converters.add(new MappingJackson2MessageConverter()); // used to handle json messages
        converters.add(new StringMessageConverter()); // used to handle raw strings
        stompClient.setMessageConverter(new CompositeMessageConverter(converters));

        // connect client to the websocket server
        StompSession session = stompClient.connectAsync(String.format(WEBSOCKET_URI, port),
                        new StompSessionHandlerAdapter() {
                        })
                // wait 1 sec for the client to be connected
                .get(1, TimeUnit.SECONDS);

        // subscribes to the topic defined in WebSocketBrokerController
        // and adds received messages to WebSocketBrokerIntegrationTest#messages
        session.subscribe(websocketTopic, new StompFrameHandlerClientImpl(messages));

        return session;
    }

    public String setUpLobby() throws Exception {
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userID", userID);
        jsonObject.put("userName", userName);
        StompSession lobbyCreationSession = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE + "/TEST_USER_ID");
        lobbyCreationSession.send(WEBSOCKET_TOPIC_CREATE_LOBBY, jsonObject);
        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assert createLobbyResponse != null;
        return createLobbyResponse;
    }

    public void setUpTwoPlayerJoinLobby(String lobbyCode) throws Exception {
        String userID = "TEST_USER_ID" + System.currentTimeMillis() / 1000;
        String userName = "TEST_USER_NAME_2";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lobbyCode", lobbyCode);
        jsonObject.put("userID", userID);
        jsonObject.put("userName", userName);
        StompSession joinPlayerSession = initStompSession(WEBSOCKET_TOPIC_JOIN_LOBBY_RESPONSE + "/" + lobbyCode + "/" + userID);
        joinPlayerSession.send(WEBSOCKET_TOPIC_JOIN_LOBBY, jsonObject);
        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assert createLobbyResponse != null;

        userID = "TEST_USER_ID" + System.currentTimeMillis() / 1000;
        userName = "TEST_USER_NAME_3";
        jsonObject = new JSONObject();
        jsonObject.put("lobbyCode", lobbyCode);
        jsonObject.put("userID", userID);
        jsonObject.put("userName", userName);
        joinPlayerSession = initStompSession(WEBSOCKET_TOPIC_JOIN_LOBBY_RESPONSE + "/" + lobbyCode + "/" + userID);
        joinPlayerSession.send(WEBSOCKET_TOPIC_JOIN_LOBBY, jsonObject);
        createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assert createLobbyResponse != null;
    }

    public void setUpStartGame(String lobbyCode) throws Exception {
        StompSession startGameSession = initStompSession(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY_RESPONSE);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lobbyCode", lobbyCode);
        startGameSession.send(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY, jsonObject);
        String startGameResponse = messages.poll(1, TimeUnit.SECONDS);
        assert startGameResponse != null;
    }

    public List<Card> setUpDealNewRound(String lobbyCode) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("lobbyCode", lobbyCode);
        payload.put("userID", "TEST_USER_ID");
        StompSession dealNewRoundSession = initStompSession(WEBSOCKET_TOPIC_DEAL_NEW_ROUND_RESPONSE + "/" + lobbyCode + "/TEST_USER_ID");
        dealNewRoundSession.send(WEBSOCKET_TOPIC_DEAL_NEW_ROUND, payload);
        String dealNewRoundResponse = messages.poll(1, TimeUnit.SECONDS);
        assert dealNewRoundResponse != null;

        HandCardsRequest handCardsRequest = objectMapper.readValue(dealNewRoundResponse, HandCardsRequest.class);
        return handCardsRequest.getHandCards();
    }
}

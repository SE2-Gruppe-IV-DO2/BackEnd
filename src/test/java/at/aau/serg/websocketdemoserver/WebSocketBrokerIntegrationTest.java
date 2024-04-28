package at.aau.serg.websocketdemoserver;

import at.aau.serg.websocketdemoserver.gamelogic.LobbyManager;
import at.aau.serg.websocketdemoserver.websocket.StompFrameHandlerClientImpl;
import net.minidev.json.JSONObject;
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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketBrokerIntegrationTest {

    @LocalServerPort
    private int port;

    private final String WEBSOCKET_URI = "ws://localhost:%d/websocket-example-broker";
    private final String WEBSOCKET_TOPIC_HELLO_RESPONSE = "/topic/hello-response";

    private final String WEBSOCKET_TOPIC_CREATE_LOBBY = "/app/create_new_lobby";
    private final String WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE = "/topic/lobby-created";

    private final String WEBSOCKET_TOPIC_JOIN_LOBBY = "/app/join_lobby";
    private final String WEBSOCKET_TOPIC_JOIN_LOBBY_RESPONSE = "/topic/lobby-joined";

    private final String WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY = "/app/start_game_for_lobby";
    private final String WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY_RESPONSE = "/topic/game_for_lobby_started";

    private final String WEBSOCKET_TOPIC_DEAL_NEW_ROUND = "/app/deal_new_round";
    private final String WEBSOCKET_TOPIC_DEAL_NEW_ROUND_RESPONSE = "/topic/new-round-dealt";

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
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE);

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
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE);

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
        StompSession session = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE);

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

        StompSession sessionJoin = initStompSession(WEBSOCKET_TOPIC_JOIN_LOBBY_RESPONSE);

        String userIDJoin = "TEST_USER_ID_2";
        String userNameJoin = "TEST_USER_NAME_2";

        JSONObject joinLobbyPayload = new JSONObject();
        joinLobbyPayload.put("lobbyCode", createLobbyResponse);
        joinLobbyPayload.put("userID", userIDJoin);
        joinLobbyPayload.put("userName", userNameJoin);

        sessionJoin.send(WEBSOCKET_TOPIC_JOIN_LOBBY, joinLobbyPayload);

        String joinLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(joinLobbyResponse).isNotEmpty();
        assertEquals(joinLobbyResponse.length(), LobbyManager.LOBBY_CODE_LENGTH);
    }

    @Test
    public void testWebSocketDealNewRound() throws Exception {
        StompSession session = initStompSession(WEBSOCKET_TOPIC_DEAL_NEW_ROUND_RESPONSE);

        // send a message to the server
        session.send(WEBSOCKET_TOPIC_DEAL_NEW_ROUND, "");

        String dealNewRoundResponse = messages.poll(1, TimeUnit.SECONDS);
        assertThat(dealNewRoundResponse).isNullOrEmpty();
    }

    @Test
    public void testWebSocketStartGameForLobby() throws Exception {

        // create a new lobby and start the game
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userID", userID);
        jsonObject.put("userName", userName);

        StompSession lobbyCreationSession = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE);
        lobbyCreationSession.send(WEBSOCKET_TOPIC_CREATE_LOBBY, jsonObject);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        System.out.println("createLobbyResponse:" + createLobbyResponse);

        assert createLobbyResponse != null;
        StompSession startGameSession = initStompSession(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY_RESPONSE);
        startGameSession.send(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY, createLobbyResponse);
        String startGameResponse = messages.poll(1, TimeUnit.SECONDS);

        assertThat(startGameResponse).isNotEmpty();
    }

    @Test
    public void testWebSocket_GetPlayerTurnUpdate() throws Exception {
        // create a new lobby and start the game
        String userID = "TEST_USER_ID";
        String userName = "TEST_USER_NAME";

        JSONObject payload = new JSONObject();
        payload.put("userID", userID);
        payload.put("userName", userName);

        StompSession lobbyCreationSession = initStompSession(WEBSOCKET_TOPIC_CREATE_LOBBY_RESPONSE);
        lobbyCreationSession.send(WEBSOCKET_TOPIC_CREATE_LOBBY, payload);

        String createLobbyResponse = messages.poll(1, TimeUnit.SECONDS);
        System.out.println("createLobbyResponse:" + createLobbyResponse);

        assert createLobbyResponse != null;
        StompSession startGameSession = initStompSession(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY_RESPONSE);
        startGameSession.send(WEBSOCKET_TOPIC_START_GAME_FOR_LOBBY, createLobbyResponse);
        String startGameResponse = messages.poll(1, TimeUnit.SECONDS);

        //TODO: Add tests if the message for the active player came
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

}

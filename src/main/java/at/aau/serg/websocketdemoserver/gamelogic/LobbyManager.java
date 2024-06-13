package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.messaging.dtos.CardPlayRequest;

import java.util.*;

public class LobbyManager {
    private static LobbyManager instance;
    private final Map<String, Lobby> lobbies;
    public static final int LOBBY_CODE_LENGTH = 6;
    public static int MAX_LOBBY_CREATION_RETRIES = 10;

    private LobbyManager() {
        lobbies = new HashMap<>();
    }

    public static synchronized LobbyManager getInstance() {
        if (instance == null) {
            instance = new LobbyManager();
        }
        return instance;
    }

    public String createLobby() throws Exception {
        Lobby newLobby = new Lobby(generateUniqueCode());
        String newlyCreatedLobbyCode = newLobby.getLobbyCode();
        lobbies.put(newlyCreatedLobbyCode, newLobby);
        return newlyCreatedLobbyCode;
    }

    public void addPlayerToLobby(String lobbyCode, String playerID, String playerName) {
        Player player = new Player(playerID, playerName);
        addPlayerToLobby(lobbyCode, player);
    }

    public void addPlayerToLobby(String lobbyCode, Player player) {
        if (!lobbies.containsKey(lobbyCode))
            throw new IllegalArgumentException("Lobby with code '" + lobbyCode + "' does not exist!");
        lobbies.get(lobbyCode).addPlayer(player);
    }

    public boolean isPlayerInLobby(String lobbyCode, String playerID) {
        if (!lobbies.containsKey(lobbyCode))
            return false;

        return lobbies.get(lobbyCode).getPlayerIDs().contains(playerID);
    }

    public List<Lobby> getAllLobbies() {
        return new ArrayList<>(lobbies.values());
    }

    public Lobby getLobbyByID(String lobbyID) {
        return lobbies.get(lobbyID);
    }

    public void startGameForLobby(String lobbyCode) {
        if (!lobbies.containsKey(lobbyCode))
            throw new IllegalArgumentException("Lobby with code '" + lobbyCode + "' does not exist!");

        Lobby selectedLobby = lobbies.get(lobbyCode);

        if (selectedLobby.isLobbyGameStarted())
            throw new IllegalStateException("Could not start game for Lobby. Game has already started!");

        selectedLobby.setLobbyGameStarted(true);
    }

    public void deleteAllLobbies() {
        lobbies.clear();
    }

    public boolean isLobbyCodeUnique(String code) {
        return !lobbies.containsKey(code);
    }

    private String generateUniqueCode() throws Exception {
        String code;
        int lobbyCreationRetryCounter = 0;
        do {
            if (lobbyCreationRetryCounter > MAX_LOBBY_CREATION_RETRIES)
                throw new Exception("Could not create new lobby. Maybe the maximal amount of lobbies is reached.");
            else
                lobbyCreationRetryCounter++;

            code = generateCode();
        } while (!isLobbyCodeUnique(code));
        return code;
    }

    public String generateCode() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, LOBBY_CODE_LENGTH).toUpperCase();
    }

    public Lobby getLobbyByCode(String code) throws Exception {
        if (!lobbies.containsKey(code)) {
            throw new Exception("Lobby not found");
        }
        return lobbies.get(code);
    }

    public void dealNewRound(String code) throws Exception {
        Lobby lobby = getLobbyByCode(code);
        lobby.getDeck().dealNewRound(lobby.getPlayers());
    }

    public Card cardPlayed(CardPlayRequest cardPlayRequest) {
        Lobby targetLobby = lobbies.get(cardPlayRequest.getLobbyCode());
        if (targetLobby == null) {
            throw new IllegalArgumentException("Lobby not found");
        }
        Player player = targetLobby.getPlayerByID(cardPlayRequest.getUserID());
        if (player == null) {
            throw new IllegalArgumentException("Player not found in the lobby: " + cardPlayRequest.getUserID());
        }
        Card c = player.playCardForPlayer(cardPlayRequest.getName(), cardPlayRequest.getColor(), Integer.valueOf(cardPlayRequest.getValue()));
        if (c == null) {
            throw new IllegalArgumentException("Card not found in player's hand");
        }
        targetLobby.addCardToTrick(cardPlayRequest.getUserID(), c);

        player.updateCheatAttempt(targetLobby.getCurrentTrick(), cardPlayRequest.getColor());

        return c;
    }

    public void endCurrentPlayersTurnForLobby(String code) throws Exception {
        Lobby lobby = getLobbyByCode(code);
        lobby.endCurrentPlayersTurn();
    }

    public Player getActivePlayerForLobby(String code) throws Exception {
        Lobby lobby = getLobbyByCode(code);
        return lobby.getActivePlayer();
    }

    public void setGaiaPlayerAsStartPlayer(String code) throws Exception {
        Lobby lobby = getLobbyByCode(code);
        lobby.setGaiaHolderAsStartPlayer();
    }
}
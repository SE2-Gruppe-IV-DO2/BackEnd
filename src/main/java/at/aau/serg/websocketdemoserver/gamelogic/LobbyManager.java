package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Deck;

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

    private Lobby findLobby(String code) throws Exception {
        if (!lobbies.containsKey(code)) {
            throw new Exception("Lobby not found");
        }
        return lobbies.get(code);
    }

    public void dealNewRound(String code) throws Exception {
        Lobby lobby = findLobby(code);
        lobby.getDeck().dealNewRound(lobby.getPlayers());
    }
}
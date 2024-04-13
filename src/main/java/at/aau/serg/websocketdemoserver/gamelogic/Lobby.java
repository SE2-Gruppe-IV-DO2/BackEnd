package at.aau.serg.websocketdemoserver.gamelogic;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private List<Player> players = new ArrayList<>();
    @Getter
    private String lobbyCode;
    public static final int MAX_PLAYER_COUNT = 5;
    public static final int MIN_PLAYER_FOR_GAME_START_COUNT = 3;

    public Lobby(String lobbyCode) {
        if (!isValid(lobbyCode)) {
            throw new IllegalArgumentException("Invalid lobby code");
        }
        this.lobbyCode = lobbyCode.trim();
    }

    public void addPlayer(Player player) {
        if (player.getPlayerID().isEmpty())
            throw new IllegalArgumentException("Invalid player ID");
        if (isFull())
            throw new IllegalStateException("Cannot add player: Lobby is full");

        players.add(player);
    }

    public List<String> getPlayerIDs() {
        List<String> playerIDs = new ArrayList<>();

        for (Player player : players) {
            playerIDs.add(player.getPlayerID());
        }

        return playerIDs;
    }

    static boolean isValid(String code) {
        return code != null && !code.trim().isEmpty();
    }

    private boolean isFull() {
        return players.size() >= MAX_PLAYER_COUNT;
    }

    public boolean isReadyToStart() {
        return players.size() >= MIN_PLAYER_FOR_GAME_START_COUNT && players.size() <= MAX_PLAYER_COUNT;
    }
}
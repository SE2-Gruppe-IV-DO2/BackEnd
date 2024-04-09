package at.aau.serg.websocketdemoserver.gamelogic;

import java.util.ArrayList;
import java.util.List;

public record Lobby(String lobbyCode) {
    private static final List<Player> players = new ArrayList<>();
    public Lobby(String lobbyCode) {
        if (!isValid(lobbyCode)) {
            throw new IllegalArgumentException("Invalid lobby code");
        }
        this.lobbyCode = lobbyCode.trim();
    }

    public void addPlayer(Player player) {
        if (player.getPlayerID().isEmpty())
            throw new IllegalArgumentException("Invalid player ID");
        players.add(player);
    }

    public List<String> getPlayerIDs() {
        List<String> playerIDs = new ArrayList<>();

        for (Player player : players) {
            playerIDs.add(player.getPlayerID());
        }

        return playerIDs;
    }

    private boolean isValid(String code) {
        return code != null && !code.trim().isEmpty();
    }
}
package at.aau.serg.websocketdemoserver.gamelogic;

public class Lobby {
    private String lobbyCode;

    public Lobby(String lobbyCode) {
        if (!isValid(lobbyCode)) {
            throw new IllegalArgumentException("Invalid lobby code");
        }
        this.lobbyCode = lobbyCode.trim();
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    private boolean isValid(String code) {
        return code != null && !code.trim().isEmpty();
    }
}
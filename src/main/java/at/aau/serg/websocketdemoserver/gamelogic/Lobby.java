package at.aau.serg.websocketdemoserver.gamelogic;

import lombok.Getter;

@Getter
public record Lobby(String lobbyCode) {
    public Lobby(String lobbyCode) {
        if (!isValid(lobbyCode)) {
            throw new IllegalArgumentException("Invalid lobby code");
        }
        this.lobbyCode = lobbyCode.trim();
    }

    private boolean isValid(String code) {
        return code != null && !code.trim().isEmpty();
    }
}
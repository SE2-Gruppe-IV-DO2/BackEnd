package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import at.aau.serg.websocketdemoserver.deckmanagement.Deck;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby {
    @Getter
    private final List<Player> players = new ArrayList<>();
    @Getter
    private final String lobbyCode;
    @Getter
    private final static int MAX_ROUNDS = 5;
    @Getter
    @Setter
    private boolean lobbyGameStarted = false;
    public static final int MAX_PLAYER_COUNT = 5;
    public static final int MIN_PLAYER_FOR_GAME_START_COUNT = 3;
    @Getter
    public Deck deck;
    @Getter
    private List<Card> currentTrick;
    @Getter
    private int indexOfActivePlayer = -1;
    @Getter
    private Map<String, HashMap<Integer, Integer>> playerPoints;
    @Getter
    private int currentRound = 0;

    public Lobby(String lobbyCode) {
        if (!isValid(lobbyCode)) {
            throw new IllegalArgumentException("Invalid lobby code");
        }
        this.deck = new Deck();
        this.lobbyCode = lobbyCode.trim();
        this.currentTrick = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        if (player.getPlayerID().isEmpty())
            throw new IllegalArgumentException("Invalid player ID");
        if (isFull())
            throw new IllegalStateException("Cannot add player: Lobby is full");

        if (players.isEmpty())
            indexOfActivePlayer = 0;

        players.add(player);
    }

    public List<String> getPlayerIDs() {
        List<String> playerIDs = new ArrayList<>();

        for (Player player : players) {
            playerIDs.add(player.getPlayerID());
        }

        return playerIDs;
    }

    public Player getPlayerByID(String playerID) {
        for (Player player : players) {
            if (player.getPlayerID().equals(playerID)) {
                return player;
            }
        }
        return null;
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

    public Player getActivePlayer() {
        if (indexOfActivePlayer == -1)
            return null;

        if (indexOfActivePlayer >= players.size())
            throw new IllegalStateException("Lobby players is in an illegal state! Index is wrong!");

        return players.get(indexOfActivePlayer);
    }

    public void endCurrentPlayersTurn() {
        indexOfActivePlayer++;

        if (indexOfActivePlayer >= players.size())
            indexOfActivePlayer = 0;
    }

    public void setGaiaHolderAsStartPlayer() {
        List<Player> players = getPlayers();
        boolean foundGaiaHolder = false;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).hasGaiaCard()) {
                foundGaiaHolder = true;
                indexOfActivePlayer = i;
            }
        }

        if (!foundGaiaHolder)
            throw new IllegalStateException("No player has the Gaia Card!");
    }

    public boolean isCurrentTrickDone() {
        return (currentTrick.size() == players.size());
    }

    public boolean isRoundFinished() {
        for (Player player : players) {
            if (!player.getCardsInHand().isEmpty() || !player.isPlayerDead()) {
                return false;
            }
        }
        calculateAndSetRoundPoints();
        return true;
    }

    public void createPointBoard() {
        playerPoints = new HashMap<>();
        for (Player player : players) {
            playerPoints.put(player.getPlayerName(), new HashMap<>());
        }
    }

    public void calculateAndSetRoundPoints() {
        for (Player player : players) {
            if (player.isPlayerDead()) {
                playerPoints.get(player.getPlayerID()).put(currentRound, -3);
            } else {
                playerPoints.get(player.getPlayerID()).put(currentRound, getLowestCardValueSum(player));
            }
        }
        currentRound++;
    }

    private int getLowestCardValueSum(Player player) {
        int sum = 0;
        for (Map.Entry<CardType, Integer> entry : player.getClaimedTricks().entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }
}
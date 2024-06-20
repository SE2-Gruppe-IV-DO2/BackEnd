package at.aau.serg.websocketdemoserver.gamelogic;

import at.aau.serg.websocketdemoserver.deckmanagement.Card;
import at.aau.serg.websocketdemoserver.deckmanagement.CardType;
import at.aau.serg.websocketdemoserver.deckmanagement.Deck;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private final Integer CHEATING_ACCUSATION_POINTS_ADJUSTMENT = 5;
    @Getter
    public Deck deck;
    @Getter
    private List<Card> currentTrick;
    @Getter
    private LinkedHashMap<String, Card> lastPlayedCardPerPlayer;
    @Getter
    private int indexOfActivePlayer = -1;
    @Getter
    @Setter
    private Map<String, HashMap<Integer, Integer>> playerPoints;
    @Getter
    private int currentRound = 1;

    public Lobby(String lobbyCode) {
        if (!isValid(lobbyCode)) {
            throw new IllegalArgumentException("Invalid lobby code");
        }
        this.deck = new Deck();
        this.lobbyCode = lobbyCode.trim();
        this.currentTrick = new ArrayList<>();
        this.lastPlayedCardPerPlayer = new LinkedHashMap<>();
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

    public List<String> getPlayerNames() {
        List<String> playerNames = new ArrayList<>();

        for (Player player : players) {
            playerNames.add(player.getPlayerName());
        }

        return playerNames;
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

    public void setPlayerAsActivePlayer(String playerId) {
        boolean foundWantedPlayer = false;

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerID().equals(playerId)) {
                foundWantedPlayer = true;
                indexOfActivePlayer = i;
            }
        }

        if (!foundWantedPlayer)
            throw new IllegalStateException("Player with id " + playerId + " could not be found!");
    }

    public boolean isCurrentTrickDone() {
        return (currentTrick.size() == players.size());
    }

    public boolean isRoundFinished() {
        for (Player player : players) {
            if (!player.getCardsInHand().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void createPointBoard() {
        playerPoints = new HashMap<>();
        for (Player player : players) {
            playerPoints.put(player.getPlayerName(), new HashMap<>());
            playerPoints.get(player.getPlayerName()).put(-1, 0);
        }
    }

    public void endRound() {
        calculateAndSetRoundPoints();
        for (Player player : players) {
            player.getCardsInHand().clear();
            player.getClaimedTricks().clear();
        }
        currentRound++;
    }

    public void calculateAndSetRoundPoints() {
        for (Player player : players) {
            HashMap<Integer, Integer> roundPoints = playerPoints.get(player.getPlayerName());
            if (player.isPlayerDead()) {
                roundPoints.put(currentRound, -3);
            } else {
                roundPoints.put(currentRound, getLowestCardValueSum(player));
            }
            playerPoints.put(player.getPlayerName(), roundPoints);
        }
    }

    public void adjustPointsAfterCheatingAccusation(Player player, boolean correctAccusation) {
        HashMap<Integer, Integer> roundPoints = playerPoints.get(player.getPlayerName());
        if (correctAccusation) {
            roundPoints.put(-1, roundPoints.get(-1) + CHEATING_ACCUSATION_POINTS_ADJUSTMENT);
        }
        else {
            roundPoints.put(-1, roundPoints.get(-1) - CHEATING_ACCUSATION_POINTS_ADJUSTMENT);
        }
    }

    private int getLowestCardValueSum(Player player) {
        int sum = 0;
        for (Map.Entry<CardType, Integer> entry : player.getClaimedTricks().entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }

    public void addCardToTrick(String playerId, Card card) {
        getCurrentTrick().add(card);
        getLastPlayedCardPerPlayer().put(playerId, card);
    }

    public HashMap<String, Map<CardType, Integer>> getPlayerTricks() {
        HashMap<String, Map<CardType, Integer>> playerTricks = new HashMap<>();
        for (Player player : players) {
            playerTricks.put(player.getPlayerName(), player.getClaimedTricks());
        }
        return playerTricks;
    }

    public void clearTrick() {
        currentTrick.clear();
        lastPlayedCardPerPlayer.clear();
    }

    public Player evaluateAndHandoutTrick() {
        //Check who won the trick
        String playerIdOfWinner = deck.evaluateWinningPlayerForRound(getLastPlayedCardPerPlayer());
        //add trick to players claimedTricks:
        Player playerThatHasWon = getPlayerByID(playerIdOfWinner);
        playerThatHasWon.addClaimedTrick(getCurrentTrick());

        clearTrick();

        //player that has won the trick should be active player for new round
        setPlayerAsActivePlayer(playerIdOfWinner);

        return playerThatHasWon;
    }

    public void resetCheatAttempts() {
        for (Player player : players) {
            player.cheatedInCurrentRound = false;
        }
    }
}
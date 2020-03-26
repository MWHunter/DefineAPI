package defineoutside.main;

import defineoutside.creator.Game;
import defineoutside.games.GameLobby;
import defineoutside.network.PlayerQueue;
import defineoutside.network.QueueData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

public class Matchmaking {
    private static HashMap<UUID, String> playersLookingForGameType = new HashMap<>();

    public void addPlayerToCentralQueue(UUID playerUUID, String gameType) {

        if (playersLookingForGameType.get(playerUUID) == null || !playersLookingForGameType.get(playerUUID).equals(gameType)) {
            QueueData sendQueueInfo = new QueueData(playerUUID, gameType);
            PlayerQueue playerQueue = new PlayerQueue();
            try {
                playerQueue.addToQueue(sendQueueInfo);
            } catch (Exception e) {
                Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.RED + "Queue > Unable to contact the central queue system (is it on fire?)");
            }
        }
    }

    public void addPlayer(UUID playerUUID, String gameType) {
        playersLookingForGameType.put(playerUUID, gameType);
        searchForGames();
    }

    public void searchForGames() {
        PlayerManager pm = new PlayerManager();
        GameManager gm = new GameManager();

        for (UUID playerUUID : playersLookingForGameType.keySet()) {
            String firstPlayerGame = playersLookingForGameType.get(playerUUID);
            List<UUID> playersInSameQueue = new ArrayList<>();

            if (firstPlayerGame != null) {
                for (UUID playersAlsoInQueue : playersLookingForGameType.keySet()) {
                    String otherPlayer = playersLookingForGameType.get(playersAlsoInQueue);
                    if (otherPlayer.equals(firstPlayerGame) && !pm.getDefinePlayer(playerUUID).isLockInGame()) {
                        playersInSameQueue.add(playersAlsoInQueue);
                    }
                }
            }

            if (playersInSameQueue.size() > 0) {
                for (Game game : gm.getGamesHashMap().values()) {
                    if (game.getGameType().equals(firstPlayerGame)) {

                        // Remove people from the queue
                        for (UUID uuid : playersInSameQueue) {
                            removePlayer(uuid);
                        }

                        gm.transferPlayers(playersInSameQueue, game);
                        break;
                    }
                }
            }
        }
    }

    public void removePlayer(UUID uuid) {
        playersLookingForGameType.remove(uuid);
    }
}

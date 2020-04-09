package defineoutside.main;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.Game;
import defineoutside.network.PlayerQueue;
import defineoutside.network.QueueData;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Matchmaking {
    private static HashMap<DefinePlayer, String> playersLookingForGameType = new HashMap<>();

    public void addPlayerToCentralQueue(DefinePlayer playerUUID, String gameType) {

        if (playersLookingForGameType.get(playerUUID) == null || !playersLookingForGameType.get(playerUUID).equals(gameType)) {
            QueueData sendQueueInfo = new QueueData(playerUUID, gameType);
            PlayerQueue playerQueue = new PlayerQueue();
            try {
                playerQueue.addToQueue(sendQueueInfo);
            } catch (Exception e) {
                playerUUID.getBukkitPlayer().sendMessage(ChatColor.RED + "Queue > Unable to contact the central queue system (is it on fire?)");
            }
        }
    }

    public void addPlayer(DefinePlayer playerUUID, String gameType) {
        playersLookingForGameType.put(playerUUID, gameType);
        searchForGames();
    }

    public void searchForGames() {

        for (DefinePlayer definePlayer : playersLookingForGameType.keySet()) {
            String firstPlayerGame = playersLookingForGameType.get(definePlayer);
            List<DefinePlayer> playersInSameQueue = new ArrayList<>();

            if (firstPlayerGame != null) {
                for (DefinePlayer playersAlsoInQueue : playersLookingForGameType.keySet()) {
                    String otherPlayer = playersLookingForGameType.get(playersAlsoInQueue);
                    if (otherPlayer.equals(firstPlayerGame) && !definePlayer.isLockInGame()) {
                        playersInSameQueue.add(playersAlsoInQueue);
                    }
                }
            }

            if (playersInSameQueue.size() > 0) {
                for (Game game : GameManager.getGamesHashMap().values()) {
                    if (game.getGameType().equals(firstPlayerGame)) {

                        // Remove people from the queue
                        for (DefinePlayer uuid : playersInSameQueue) {
                            removePlayer(uuid);
                        }

                        GameManager.transferPlayers(playersInSameQueue, game);
                        break;
                    }
                }
            }
        }
    }

    public void removePlayer(DefinePlayer uuid) {
        playersLookingForGameType.remove(uuid);
    }
}

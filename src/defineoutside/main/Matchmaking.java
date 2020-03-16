package defineoutside.main;

import defineoutside.creator.Game;
import defineoutside.games.GameLobby;
import defineoutside.network.PlayerQueue;
import defineoutside.network.QueueData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

        /*if (playersLookingForGameType.get(playerUUID) == null || !playersLookingForGameType.get(playerUUID).equals(gameType)) {
            QueueData sendQueueInfo = new QueueData(playerUUID, gameType);
            PlayerQueue playerQueue = new PlayerQueue();
            try {
                playerQueue.addToQueue(sendQueueInfo);
            } catch (Exception e) {
                Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.RED + "Queue > Unable to contact the central queue system (is it on fire?)");
            }
        }*/

        playersLookingForGameType.put(playerUUID, gameType);
        searchForGames();
    }

    public void searchForGames() {
        PlayerManager pm = new PlayerManager();
        GameManager gm = new GameManager();

        // TODO: Not hard coded player minimum
        // TODO: A bit more optimization
        for (UUID playerUUID : playersLookingForGameType.keySet()) {
            List<UUID> playersFoundGame = new ArrayList<>();
            String gametype = playersLookingForGameType.get(playerUUID);

            if (gametype != null) {

                // This can be the same person
                for (UUID playerUUID2 : playersLookingForGameType.keySet()) {
                    // Player is locked while transferring between games
                    // Previously players could create ~20 games while in the process of being transferred, basically causing a five second lagspike
                    // Thankfully this was caught in testing, whatever that is.
                    if (playersLookingForGameType.get(playerUUID2).equals(gametype) && !pm.getDefinePlayer(playerUUID).isLockInGame()) {
                        playersFoundGame.add(playerUUID2);
                    }
                }

                // TODO: Check if there is capacity in the server
                if (playersFoundGame.size() > 0) {

                    Game gameFound = null;

                    // This is meant for connecting to game lobbies before connecting to something else
                    if (gametype.contains("gamelobby")) {
                        String subtype = gametype.substring(gametype.indexOf(":") + 2);

                        for (UUID game : gm.getGamesHashMap().keySet()) {
                            // If looped games equals searching game
                            // Must cast the game to game lobby to get the type of lobby it is
                            // Need a check, or else an exception will occur
                            if (gm.getGamesHashMap().get(game) instanceof GameLobby) {

                                GameLobby gl = (GameLobby) gm.getGamesHashMap().get(game);
                                // Any means that it is a new lobby
                                if (gl.getLobbyForGametype().equalsIgnoreCase(subtype)) {
                                    gameFound = gm.getGamesHashMap().get(game);
                                    break;
                                }

                                /*if (gl.getLobbyForGametype().equals("any")) {
                                    gameFound = gm.getGamesHashMap().get(game);
                                    gl.setLobbyForGametype(gametype.substring(gametype.indexOf(":") + 2));
                                    break;
                                }*/
                            }
                        }
                    } else {
                        // This is meant for connecting directly to games, instead of lobbies, such as in the case of duels
                        // TODO: Optimize this function, somehow, for a larger scale sometime later, when it needs to be optimized
                        for (UUID game : gm.getGamesHashMap().keySet()) {

                            // If looped games equals searching game
                            if (gm.getGamesHashMap().get(game).getGameType().equals(gametype) && (gm.getGamesHashMap().get(game).isAllowPlayersToJoinNow() || gm.getGamesHashMap().get(game).isAlwaysAllowPlayersJoin())) {
                                gameFound = gm.getGamesHashMap().get(game);
                                break;
                            }
                        }
                    }

                    // TODO: Allow players to join games in progress
                    if (playersFoundGame.size() >= gm.getMinPlayers(gametype)) {
                        // We need to make a game
                        if (gameFound == null) {
                            gameFound = gm.createLocalGame(gametype);
                        }

                        // Just throw players in a new gamelobby
                        if (gameFound == null) {
                            gameFound = gm.createLocalGame("gamelobby");
                            ((GameLobby) gameFound).setLobbyForGametype(gametype.substring(gametype.indexOf(":") + 2));
                        }

                        // And enforce the player limit
                        if (playersFoundGame.size() > gameFound.getMaxPlayers()) {
                            playersFoundGame = playersFoundGame.subList(0, gameFound.getMaxPlayers() - gameFound.getUuidParticipating().size());
                        }

                        // Remove people from the queue
                        for (UUID uuid : playersFoundGame) {
                            removePlayer(uuid);
                        }

                        gm.transferPlayers(playersFoundGame, gameFound);
                        // Must break, or else there is a concurrent modification exception.  Shouldn't be an issue.
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

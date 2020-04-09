package defineoutside.main;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.DefineWorld;
import defineoutside.creator.Game;
import defineoutside.games.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class GameManager {
    // The name of the map must be the name of the game.
    private static HashMap<UUID, Game> completeGamesMap = new HashMap<UUID, Game>();
    private static HashMap<UUID, DefineWorld> completeArenasMap = new HashMap<UUID, DefineWorld>();
    private static HashMap<World, Game> getGameFromBukkitWorld = new HashMap<World, Game>();

    // TODO: This will be responsible for the specific type of game to register, but that is later
    // TODO: This is responsible for configs and stuff

    public static Game createLocalGame(String gameType) {

        Game game;
        DefineWorld dw;

        ConfigManager cm = new ConfigManager();

        switch (gameType) {
            case "gamelobby":
                game = new GameLobby();

                // Make it get the world spawn points from config, the kit config, and then register the game with gamemanager (this class!)
                game.setSpawnKitName(cm.getRandomKitSelectorName(gameType));
                if (game.getSpawnKitName() == null) {
                    game.setSpawnKitName(cm.getRandomKitName(gameType));
                } else {
                    game.setSpawnItemList(cm.getKit(game.getSpawnKitName()));
                }

                game.setWorldFolder(cm.getRandomMap(gameType));

                // Make it get the world spawn points from config, the kit config, and then register the game with gamemanager (this class!)
                game.createGameWorldAndRegisterGame();

                // Begin to create the world, async
                dw = game.getGameWorld();
                dw.createArena(game.getGameUUID().toString(), game.getWorldFolder());

                // When the world is ready, register it.  Not ready instantly since async
                new BukkitRunnable() {
                    public void run() {
                        if (dw.isReady()) {
                            registerWorld(game.getGameUUID(), dw);
                            cancel();
                        }
                    }
                }.runTaskTimer(MainAPI.getPlugin(), 0L, 5L);

                return game;
            case "bedwars":
                game = new Bedwars();

                game.setSpawnKitName(cm.getRandomKitSelectorName(gameType));
                if (game.getSpawnKitName() == null) {
                    game.setSpawnKitName(cm.getRandomKitName(gameType));
                } else {
                    game.setSpawnItemList(cm.getKit(game.getSpawnKitName()));
                }

                game.setWorldFolder(cm.getRandomMap("bedwars"));

                // Make it get the world spawn points from config, the kit config, and then register the game with gamemanager (this class!)
                game.createGameWorldAndRegisterGame();

                // Begin to create the world, async
                dw = game.getGameWorld();
                dw.createArena(game.getGameUUID().toString(), game.getWorldFolder());

                // When the world is ready, register it.  Not ready instantly since async
                new BukkitRunnable() {
                    public void run() {
                        if (dw.isReady()) {
                            registerWorld(game.getGameUUID(), dw);
                            cancel();
                        }
                    }
                }.runTaskTimer(MainAPI.getPlugin(), 0L, 5L);

                return game;
            case "lobby":
                game = new Lobby();
                game.setWorldFolder(new File("world"));
                game.createGameWorldAndRegisterGame();

                return game;
                // something else
        }
        return null;
    }

    public static int getMinPlayers(String gametype) {
        switch (gametype) {
            case "bedwars":
                return 2;
        }
        return -1;
    }

    public static int getMaxTeams(String gametype) {
        switch (gametype) {
            case "bedwars":
                return 4;
        }
        return Integer.MAX_VALUE;
    }

    public static void registerGame(UUID uuid, Game gameObject) {
        completeGamesMap.put(uuid, gameObject);
        Bukkit.getLogger().log(Level.SEVERE, uuid + " has been added to gameslist.  There are currently " + completeGamesMap.size() + " games");
    }

    public static void registerWorld(UUID uuid, DefineWorld defineWorld) {
        completeArenasMap.put(uuid, defineWorld);
        getGameFromBukkitWorld.put(defineWorld.getBukkitWorld(), completeGamesMap.get(uuid));
    }

    public static void deleteGame(UUID uuid) {
        completeGamesMap.remove(uuid);
        Bukkit.getLogger().log(Level.SEVERE, uuid + " has been removed gameslistThere are currently " + completeGamesMap.size() + " games");
    }

    public static void deleteWorld(UUID uuid) {
        completeArenasMap.remove(uuid);
        getGameFromBukkitWorld.remove(Bukkit.getWorld(uuid));
    }

    public static Game getGameFromWorld(World world) {
        return getGameFromBukkitWorld.get(world);
    }

    public static HashMap<UUID, Game> getGamesHashMap() {
        return completeGamesMap;
    }

    public static HashMap<UUID, DefineWorld> getWorldsHashMap() {
        return completeArenasMap;
    }

    // Quickly transfer all active players from one game to another
    public static void transferPlayers(Game fromGame, Game toGame) {
        transferPlayers(fromGame.getUuidParticipating(), toGame);
    }

    // Quickly transfer some active players from one game to another
    public static void transferPlayers(List<DefinePlayer> uuidPlayers, Game toGame) {

        for (DefinePlayer definePlayer : uuidPlayers) {
            definePlayer.setInGameType(toGame.getGameType());
            definePlayer.setLockInGame(true);
        }

        new BukkitRunnable() {
            public void run() {
                // Takes some time due to world preparation
                if (toGame.getGameWorld().isReady()) {
                    // Since this is a reference back to the game we are transferring, we need to clone the list
                    List<DefinePlayer> uuidPlayersClone = new ArrayList<>(uuidPlayers);

                    // Prevent the game from starting before all players are transferred
                    toGame.setCanGameStart(false);

                    // Move ALL players (From cloned list)
                    for (DefinePlayer definePlayer : uuidPlayersClone) {
                        definePlayer.setLockInGame(false);

                        // Remove player from every game
                        for (Map.Entry<UUID, Game> game : GameManager.getGamesHashMap().entrySet()) {
                            if (GameManager.getGamesHashMap().get(game.getKey()).getUuidParticipating().contains(definePlayer)) {
                                GameManager.getGamesHashMap().get(game.getKey()).playerLeave(definePlayer);
                            }
                        }

                        toGame.playerJoin(definePlayer);
                    }

                    // And then try to start it, letting it choose whether it wants to
                    toGame.setCanGameStart(true);
                    toGame.attemptStart();
                    cancel();
                }
            }
        }.runTaskTimer(MainAPI.getPlugin(), 0L, 1L);
    }
}
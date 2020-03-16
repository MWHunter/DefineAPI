package defineoutside.main;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.DefineWorld;
import defineoutside.creator.Game;
import defineoutside.games.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class GameManager {
    // The name of the map must be the name of the game.
    private static HashMap<UUID, Game> completeGamesMap = new HashMap<UUID, Game>();
    private static HashMap<UUID, DefineWorld> completeArenasMap = new HashMap<UUID, DefineWorld>();
    private static HashMap<World, Game> getGameFromBukkitWorld = new HashMap<World, Game>();

    Random rand = new Random();

    public String mainGameType = "gamelobby";

    // TODO: This will be responsible for the specific type of game to register, but that is later
    // TODO: This is responsible for configs and stuff

    public Game createLocalGame(String gameType) {
        FileConfiguration mapsConfig;
        File listOfMaps;
        List<String> mapsList;
        String mapSelected;

        FileConfiguration kitConfig;
        File kitList;
        List<String> kitsList;
        String kitSelected;

        Game game;
        DefineWorld dw;

        ConfigManager cm = new ConfigManager();

        switch (gameType) {
            case "duel":
                game = new Duel();

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

            case "pit":
                game = new Pit();

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
            case "defuse":
                game = new Defuse();

                game.setSpawnKitName(cm.getRandomKitSelectorName("defuse"));
                if (game.getSpawnKitName() == null) {
                    game.setSpawnKitName(cm.getRandomKitName("defuse"));
                } else {
                    game.setSpawnItemList(cm.getKit(game.getSpawnKitName()));
                }

                game.setWorldFolder(cm.getRandomMap("defuse"));

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

    public int getMinPlayers(String gametype) {
        switch (gametype) {
            case "duel":
                return 2;
            case "pit":
                return 1;
            case "defuse":
                return 2;
            case "gamelobby":
                return 1;
        }
        return -1;
    }

    public void registerGame(UUID uuid, Game gameObject) {
        completeGamesMap.put(uuid, gameObject);
        Bukkit.getLogger().log(Level.SEVERE, uuid + " has been added to gameslist.  There are currently " + completeGamesMap.size() + " games");
    }

    public void registerWorld(UUID uuid, DefineWorld defineWorld) {
        completeArenasMap.put(uuid, defineWorld);
        getGameFromBukkitWorld.put(defineWorld.getBukkitWorld(), completeGamesMap.get(uuid));
    }

    public void deleteGame(UUID uuid) {
        completeGamesMap.remove(uuid);
        Bukkit.getLogger().log(Level.SEVERE, uuid + " has been removed gameslistThere are currently " + completeGamesMap.size() + " games");
    }

    public void deleteWorld(UUID uuid) {
        completeArenasMap.remove(uuid);
        getGameFromBukkitWorld.remove(Bukkit.getWorld(uuid));
    }

    public Game getGameFromWorld(World world) {
        return getGameFromBukkitWorld.get(world);
    }

    public HashMap<UUID, Game> getGamesHashMap() {
        return completeGamesMap;
    }

    public HashMap<UUID, DefineWorld> getWorldsHashMap() {
        return completeArenasMap;
    }

    // Quickly transfer all active players from one game to another
    public void transferPlayers(Game fromGame, Game toGame) {
        transferPlayers(fromGame.getUuidParticipating(), toGame);
    }

    // Quickly transfer some active players from one game to another
    public void transferPlayers(List<UUID> uuidPlayers, Game toGame) {

        GameManager gm = new GameManager();
        PlayerManager pm = new PlayerManager();

        for (UUID playerUUID : uuidPlayers) {
            pm.getDefinePlayer(playerUUID).setInGameType(toGame.getGameType());
            pm.getDefinePlayer(playerUUID).setLockInGame(true);
        }

        // Takes some time due to world preparation
        new BukkitRunnable() {
            public void run() {
                int playersToTransfer = uuidPlayers.size();

                if (toGame.getGameWorld().isReady()) {
                    // Prevent the game from starting before all players are transferred
                    toGame.setCanGameStart(false);

                    // Move ALL players
                    for (int x = 0; x < playersToTransfer; x++) {
                        // TODO: Less of a hack than current
                        UUID playerUUID = uuidPlayers.get(0);
                        Bukkit.broadcastMessage(ChatColor.RED + "" + playerUUID + " is being transfererered");

                        // Unlock the player to transfer him
                        PlayerManager pm = new PlayerManager();
                        DefinePlayer dp = pm.getDefinePlayer(playerUUID);
                        dp.setLockInGame(false);

                        // Remove players from every game
                        for (Map.Entry<UUID, Game> game : gm.getGamesHashMap().entrySet()) {
                            if (gm.getGamesHashMap().get(game.getKey()).getUuidParticipating().contains(playerUUID)) {
                                gm.getGamesHashMap().get(game.getKey()).playerLeave(playerUUID);
                            }
                        }

                        toGame.playerJoin(playerUUID);
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

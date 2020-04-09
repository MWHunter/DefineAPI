package defineoutside.main;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import defineoutside.creator.DefinePlayer;
import defineoutside.creator.DefineTeam;
import defineoutside.creator.Game;
import defineoutside.games.GameLobby;
import defineoutside.games.Lobby;
import defineoutside.listener.*;
import defineoutside.network.PlayerQueue;
import defineoutside.network.RegisterServer;
import defineoutside.network.SendStatistics;
import defineoutside.network.receivePlayerTransferAndCommands;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class MainAPI extends JavaPlugin implements Listener, PluginMessageListener {

    private static Plugin plugin;

    // TODO: Change this
    public static String internalServerIdentifier;
    //private ProtocolManager protocolManager;
    public static String lobbyType;

    public static String hostName = "127.0.0.1";

    public static int globalPlayers = 0;

    public boolean changedLobbyGamemode = false;

    public Random random;

    public void onEnable() {
        plugin = this;

        // I'll recommend paper even to myself
        PaperLib.suggestPaper(this);

        // Make the plugin data folder
        File folder = new File(getDataFolder() + File.separator + "arenas");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // This plugin breaks on reloads, but might as well make future code work
        // somewhat with reloads
        // Having players log into another world may break things.
        if (Bukkit.getOnlinePlayers().size() == 0) {
            try {

                // Cleaning is just deleting all files in it, without deleting the directory
                File mainWorld = new File(getServer().getWorldContainer() + File.separator + "world");
                if (mainWorld.exists()) {
                    FileUtils.deleteDirectory(mainWorld);
                }

                // Delete all root folders with a UUID in it's name
                for (File file : Bukkit.getWorldContainer().listFiles()) {

                    if (file.getParentFile().equals(Bukkit.getWorldContainer())) {
                        if (file.getName().matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                            FileUtils.deleteQuietly(file);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ConfigManager cm = new ConfigManager();
        cm.loadConfigs();

        // Events to do: player respawn, player chat, player combat, player
        // interact, player move

        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockFormListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockMeltListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockSpreadListener(), this);
        Bukkit.getPluginManager().registerEvents(new FoodChangeListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemDropListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemDurabilityChangeListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEnterBedListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new VehicleExitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerPortalListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerShootProjectile(), this);
        Bukkit.getPluginManager().registerEvents(new WeatherChangeListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageEntityListener(), this);
        Bukkit.getPluginManager().registerEvents(new DoubleJumpListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldInitEvent(), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        File main = new File(getPlugin().getDataFolder() + File.separator + "main.yml");
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(main);
        lobbyType = fileConfiguration.getString("Mainworld");

        File fromFolder = new File(getPlugin().getDataFolder() + File.separator + "lobbies" + File.separator + lobbyType);
        File toFolder = getServer().getWorldContainer();

        try {
            FileUtils.copyDirectory(fromFolder, toFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Gets the persistant hostname
        // Persistent so we don't get memory leaks from restarting servers
        File persistentName = new File(getPlugin().getDataFolder() + File.separator + "serveruuid.yml");
        FileConfiguration persistent = YamlConfiguration.loadConfiguration(persistentName);
        internalServerIdentifier = persistent.getString("UUID");

        if (internalServerIdentifier == null) {
            String randomName = UUID.randomUUID().toString();

            internalServerIdentifier = randomName;

            persistent.set("UUID", randomName);
            try {
                persistent.save(persistentName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (Bukkit.getOnlinePlayers().size() != 0) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("PlayerCount");
                out.writeUTF("ALL");

                Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

                player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
            }
        }, 0, 100);

        // Every 15 seconds return any open lobbies without players to be open
        getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), () -> {
            // Stop immediately changing the gamelobby back
            if (changedLobbyGamemode) {
                changedLobbyGamemode = false;
            } else {
                for (Game game : GameManager.getGamesHashMap().values()) {
                    if (game instanceof GameLobby) {
                        GameLobby gameLobby = (GameLobby) game;
                        if (gameLobby.getUuidParticipating().size() == 0) {
                            gameLobby.setLobbyForGametype("");
                        }
                    }
                }
            }

        }, 300, 300);
    }

    // Register the custom void generator
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new VoidWorldGenerator();
    }

    public void onDisable() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (label.equalsIgnoreCase("debugmg")) {
            GameManager gm = new GameManager();

            for (Map.Entry game : gm.getGamesHashMap().entrySet()) {
                sender.sendMessage(game.getValue().toString());
            }

            sender.sendMessage(ChatColor.RED + "Debug tool.  Abuse or spam will lead to a temporary ban.");
        }

        if (label.equalsIgnoreCase("debugworld")) {
            for (World world : Bukkit.getServer().getWorlds()) {
                sender.sendMessage(world.toString());
            }
        }

        if (sender instanceof Player && label.equalsIgnoreCase("whatgame")) {
            DefinePlayer definePlayer = PlayerManager.getDefinePlayer(((Player) sender).getUniqueId());

            for (Map.Entry<UUID, Game> entry : GameManager.getGamesHashMap().entrySet()) {
                if (entry.getValue().getUuidParticipating().contains(PlayerManager.getDefinePlayer(((Player) sender).getUniqueId()))) {
                    sender.sendMessage("You are in game: " + entry.getValue().toString());
                    sender.sendMessage("Are you alive? " + definePlayer.isAlive());
                    sender.sendMessage("You are on team " + definePlayer.getPlayerDefineTeam().toString());
                }
            }

            sender.sendMessage(ChatColor.RED + "Debug tool.  Abuse or spam will lead to a temporary ban.");
        }

        if (label.equalsIgnoreCase("whatteam")) {
            for (Game game : GameManager.getGamesHashMap().values()) {
                for (DefineTeam defineTeam : game.getUuidDefineTeams()) {
                    sender.sendMessage(defineTeam.getUuidInTeam().toString() + " is team name " + defineTeam.getName());
                }
            }
        }

        if (label.equalsIgnoreCase("leave") || label.equalsIgnoreCase("hub") || label.equalsIgnoreCase("lobby")) {
            Matchmaking mm = new Matchmaking();
            mm.addPlayerToCentralQueue(PlayerManager.getDefinePlayer(((Player) sender).getUniqueId()), "lobby");
        }

        if (label.equalsIgnoreCase("joinqueue")) {
            MainAPI.getPlugin().getServer().getScheduler().runTaskAsynchronously(MainAPI.getPlugin(), () -> {
                if (args.length != 0) {
                    Matchmaking mm = new Matchmaking();
                    mm.addPlayerToCentralQueue(PlayerManager.getDefinePlayer(((Player) sender).getUniqueId()), args[0]);
                }
            });
        }

        if (label.equalsIgnoreCase("reloadconfigs")) {
            if (sender.hasPermission("DefineAPI.reload")) {
                ConfigManager cm = new ConfigManager();
                cm.loadConfigs();
            } else {
                sender.sendMessage(ChatColor.RED + "I'm sorry, Dave, but I cannot let you do that.");
            }
        }

        if (label.equalsIgnoreCase("getuuid")) {
            sender.sendMessage("Server ID: " + internalServerIdentifier);
        }

        if (label.equalsIgnoreCase("setgametype")) {
            if (sender.hasPermission("DefineAPI.manage") && args.length != 0) {
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    File file = new File(getPlugin().getDataFolder() + File.separator + "main.yml");
                    FileConfiguration mainConfig = YamlConfiguration.loadConfiguration(file);
                    mainConfig.set("Mainworld", args[0]);
                    try {
                        mainConfig.save(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        // TODO: Support multiple games in each lobby (potentially for 1v1 arena?)
        if (label.equalsIgnoreCase("setgamemode")) {
            if (sender.hasPermission("DefineAPI.manage") && args.length != 0) {
                for (Game iteratedGame : GameManager.getGamesHashMap().values()) {
                    if (iteratedGame instanceof GameLobby && iteratedGame.getUuidParticipating().size() == 0) {
                        changedLobbyGamemode = true;

                        ((GameLobby) iteratedGame).setLobbyForGametype(args[0]);
                        getPlugin().getLogger().log(Level.WARNING, "Set a gamelobby gamemode to " + args[0]);

                        break;
                    }
                }
            }
            getServer().getLogger().log(Level.SEVERE, "Set the gametype to " + args[0]);
        }

        if (label.equalsIgnoreCase("getgamemode")) {
            for (Game game : GameManager.getGamesHashMap().values()) {
                if (game instanceof GameLobby) {
                    Bukkit.getLogger().log(Level.WARNING, "Lobby is in queue for " + ((GameLobby) game).getLobbyForGametype());
                }
            }
        }

        // TODO: Force players out to an updated server without them knowing
        if (label.equalsIgnoreCase("shutdownforupdate")) {
            if (sender.hasPermission("DefineAPI.shutdown")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Bukkit.getServer().getOnlinePlayers().size() == 0) {
                            Bukkit.shutdown();
                        }
                    }
                }.runTaskTimer(plugin, 100L, 100L/*(long)random.nextInt(18000), /*18000*/); // Try to balance the load of updating over 15 minutes
            }
        }

        return false;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static String getInternalServerIdentifier() {
        return internalServerIdentifier;
    }

    public static void loadPostWorld() {
        File main = new File(getPlugin().getDataFolder() + File.separator + "main.yml");
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(main);
        String lobbyType = fileConfiguration.getString("Mainworld");

        // TODO: Change this when needed
        if (lobbyType.equalsIgnoreCase("lobby")) {
            Game gameOverworld = new Lobby();
            gameOverworld.setWorldFolder(new File("world"));
            gameOverworld.createGameWorldAndRegisterGame();
        } else {
            Game gameOverworld = new GameLobby();
            gameOverworld.setWorldFolder(new File("world"));
            gameOverworld.createGameWorldAndRegisterGame();
        }

        // Connect to the mainframe (pls don't hack the mainframe) (mainframe ready when UUID set)
        // Due to monitoring stuff, this has to be after all games are created
        new BukkitRunnable() {
            @Override
            public void run() {
                while (true) {

                    if (internalServerIdentifier != null) {
                        SendStatistics sendStatistics = new SendStatistics();
                        sendStatistics.startNetworkMonitoring(hostName);

                        PlayerQueue playerQueue = new PlayerQueue();
                        playerQueue.ConnectToMainframe(hostName);

                        receivePlayerTransferAndCommands playerTransferAndCommands = new receivePlayerTransferAndCommands();
                        playerTransferAndCommands.ConnectToMainframe(hostName);

                        RegisterServer.sendHostname();

                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(MainAPI.getPlugin());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("PlayerCount")) {
            String server = in.readUTF();
            globalPlayers = in.readInt();
        }
    }
}

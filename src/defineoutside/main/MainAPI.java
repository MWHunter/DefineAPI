package defineoutside.main;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import defineoutside.creator.Game;
import defineoutside.games.GameLobby;
import defineoutside.games.Lobby;
import defineoutside.listener.*;
import defineoutside.network.*;
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

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MainAPI extends JavaPlugin implements Listener, PluginMessageListener {

    private static Plugin plugin;

    // TODO: Change this
    public static String internalServerIdentifier;
    //private ProtocolManager protocolManager;
    public static String lobbyType;

    public static int globalPlayers = 0;

    public boolean changedLobbyGamemode = false;

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
        getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
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

        }, 300);
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

        if (label.equalsIgnoreCase("whatgame")) {
            GameManager gm = new GameManager();
            PlayerManager pm = new PlayerManager();

            for (Map.Entry game : gm.getGamesHashMap().entrySet()) {
                if (gm.getGamesHashMap().get(game.getKey()).getUuidParticipating().contains(Bukkit.getPlayer(sender.getName()).getUniqueId())) {
                    sender.sendMessage("You are in game: " + game.getValue().toString());
                    sender.sendMessage("Are you alive? " + pm.getDefinePlayer(((Player) sender).getUniqueId()).isAlive());
                    sender.sendMessage("You are on team " + pm.getDefinePlayer((Player) sender).getPlayerDefineTeam().getName());
                }
            }

            sender.sendMessage(ChatColor.RED + "Debug tool.  Abuse or spam will lead to a temporary ban.");
        }

        if (label.equalsIgnoreCase("leave") || label.equalsIgnoreCase("hub") || label.equalsIgnoreCase("lobby")) {
            Matchmaking mm = new Matchmaking();
            mm.addPlayerToCentralQueue(((Player) sender).getUniqueId(), "lobby");
        }

        if (label.equalsIgnoreCase("joinqueue")) {
            MainAPI.getPlugin().getServer().getScheduler().runTaskAsynchronously(MainAPI.getPlugin(), () -> {
                if (args.length != 0) {
                    Matchmaking mm = new Matchmaking();
                    mm.addPlayerToCentralQueue(((Player) sender).getUniqueId(), args[0]);
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

        // Somewhat hacky solution to set the UUID used for tracking this server, using bungeecord commands (Should be a reliable hack)
        /*if (label.equalsIgnoreCase("setuuid")) {
            if (!(sender instanceof Player)) {
                getLogger().log(Level.WARNING, "Set UUID to " + args[0]);
                internalServerIdentifier = args[0];
            } else {
                sender.sendMessage(ChatColor.RED + "I'm sorry, Dave, but I cannot let you do that.");
            }
        }*/

        if (label.equalsIgnoreCase("getuuid")) {
            sender.sendMessage("Server ID: " + internalServerIdentifier);
        }

        if (label.equalsIgnoreCase("setgametype")) {
            if (sender.hasPermission("DefineAPI.manage") && args.length != 0) {
                Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(getPlugin().getDataFolder() + File.separator + "main.yml");
                        FileConfiguration mainConfig = YamlConfiguration.loadConfiguration(file);
                        mainConfig.set("Mainworld", args[0]);
                        try {
                            mainConfig.save(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        // TODO: Support multiple games in each lobby (potentially for 1v1 arena?)
        if (label.equalsIgnoreCase("setgamemode")) {
            if (sender.hasPermission("DefineAPI.manage") && args.length != 0) {
                for (Game iteratedGame : GameManager.getGamesHashMap().values()) {
                    if (iteratedGame instanceof GameLobby && ((GameLobby) iteratedGame).getLobbyForGametype().equalsIgnoreCase("")) {
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
                        sendStatistics.startNetworkMonitoring("192.168.1.196");

                        PlayerQueue playerQueue = new PlayerQueue();
                        playerQueue.ConnectToMainframe("192.168.1.196");

                        receivePlayerTransferAndCommands playerTransferAndCommands = new receivePlayerTransferAndCommands();
                        playerTransferAndCommands.ConnectToMainframe("192.168.1.196");

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

        /*DisabledDimension gameNether = new DisabledDimension();
        gameNether.setGameType("world_nether");
        gameNether.createGameWorldAndRegisterGame();

        DisabledDimension gameEnd = new DisabledDimension();
        gameEnd.setGameType("world_the_end");
        gameEnd.createGameWorldAndRegisterGame();*/
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        globalPlayers = in.readInt();
    }
}

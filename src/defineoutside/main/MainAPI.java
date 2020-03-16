package defineoutside.main;

import defineoutside.creator.Game;
import defineoutside.games.DisabledDimension;
import defineoutside.games.Lobby;
import defineoutside.listener.*;
import defineoutside.network.PlayerQueue;
import defineoutside.network.SendStatistics;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class MainAPI extends JavaPlugin implements Listener {

    private static Plugin plugin;

    public static String internalServerIdentifier;
    //private ProtocolManager protocolManager;

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
                FileUtils.cleanDirectory(new File(
                        getServer().getWorldContainer() + File.separator + "world" + File.separator + "playerdata"));

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

        Game gameOverworld = new Lobby();
        gameOverworld.setWorldFolder(new File("world"));
        gameOverworld.createGameWorldAndRegisterGame();

        DisabledDimension gameNether = new DisabledDimension();
        gameNether.setGameType("world_nether");
        gameNether.createGameWorldAndRegisterGame();

        DisabledDimension gameEnd = new DisabledDimension();
        gameEnd.setGameType("world_the_end");
        gameEnd.createGameWorldAndRegisterGame();


        // Connect to the mainframe (pls don't hack the mainframe) (mainframe ready when UUID set)
        new BukkitRunnable() {
            @Override
            public void run() {
                // The internal identifer is a hack that allows
                // This would also be used when we eventually move away from using sub servers?  Not sure when or why that will happen
                // I kind of like subservers as a way to interface with bungeecord, allowing for dynamic scaling
                // (As in no restart, not automatically buying servers (My code would have a bug that would buy 1000 servers at once anyways))
                // I'll try to replace the getting maps part of it or get the author to improve download times
                // Then I'll replace the host part or just fork it, I'll do that later
                while (true) {
                    if (internalServerIdentifier != null) {
                        SendStatistics sendStatistics = new SendStatistics();
                        sendStatistics.startNetworkMonitoring("192.168.1.196");

                        PlayerQueue playerQueue = new PlayerQueue();
                        playerQueue.ConnectToMainframe("192.168.1.196");

                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(this);
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
                    sender.sendMessage("You are on team " + pm.getDefinePlayer((Player) sender).getPlayerTeam().getName());
                }
            }

            sender.sendMessage(ChatColor.RED + "Debug tool.  Abuse or spam will lead to a temporary ban.");
        }

        if (label.equalsIgnoreCase("leave") || label.equalsIgnoreCase("hub") || label.equalsIgnoreCase("lobby")) {
            Matchmaking mm = new Matchmaking();
            mm.addPlayer(((Player) sender).getUniqueId(), "lobby");
        }

        if (label.equalsIgnoreCase("joinqueue")) {
            if (args.length != 0) {
                Matchmaking mm = new Matchmaking();
                mm.addPlayer(((Player) sender).getUniqueId(), args[0]);
            }
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
        if (label.equalsIgnoreCase("setuuid")) {
            if (!(sender instanceof Player)) {
                getLogger().log(Level.WARNING, "Set UUID to " + args[0]);
                internalServerIdentifier = args[0];
            } else {
                sender.sendMessage(ChatColor.RED + "I'm sorry, Dave, but I cannot let you do that.");
            }
        }

        if (label.equalsIgnoreCase("getuuid")) {
            sender.sendMessage("Server ID: " + internalServerIdentifier);
        }

        if (label.equalsIgnoreCase("setgametype")) {
            if (sender.hasPermission("DefineAPI.manage")) {

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
}

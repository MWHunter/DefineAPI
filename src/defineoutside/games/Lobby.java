package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.DefineWorld;
import defineoutside.creator.Game;
import defineoutside.main.*;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.logging.Level;

public class Lobby extends Game {
    private String nextGame;

    private boolean isStarting = false;

    public Location joinServerLocation = new Location(Bukkit.getWorld("world"), 0, 61 ,0);

    @Override
    public void createGameWorldAndRegisterGame() {
        ConfigManager cm = new ConfigManager();

        // Set game type before so it loads the right configs
        setGameType("lobby");
        super.createGameWorldAndRegisterGame();

        DefineWorld dwWorld = new DefineWorld();
        dwWorld.createArena("world", null);
        setGameWorld(dwWorld);

        setAllowPlayerMoveOnJoin(true);
        setAllowPlayersToJoinNow(true);
        setAlwaysAllowPlayersJoin(true);
        setAllowPlayerDamage(false);
        setAllowPlayerShootProjectile(false);
        setMinPlayers(0);
        setMaxPlayers(99999);

        GameManager gm = new GameManager();
        gm.registerGame(getGameUUID(), this);
        gm.registerWorld(getGameUUID(), dwWorld);

        spawnItemList = cm.getRandomKit(getGameType());
    }

    // TODO: This gets called twice for some reason.
    @Override
    public void playerLeave(UUID player) {
        uuidParticipating.remove(player);
    }

    @Override
    public void playerLeave(Player player) {
        playerLeave(player.getUniqueId());
    }

    @Override
    public boolean checkEndByEliminations() {
        return false;
    }

    @Override
    public void end() {
        //no end
    }

    @Override
    public void attemptStart() {
        // This is the main lobby, game lobbies will override this
    }

    @Override
    public void playerJoin(UUID player) {
        PlayerManager pm = new PlayerManager();

        DefinePlayer dp = pm.getDefinePlayer(player);

        uuidParticipating.add(player);

        dp.setPlayerTeam(getBestTeam());
        dp.setKit(getSpawnKitName());

        playerLoad(player);

        attemptStart();
    }

    @Override
    public void playerRespawn(UUID uuid, boolean canMove, boolean kitSelector) {
        PlayerManager pm = new PlayerManager();
        ConfigManager cm = new ConfigManager();
        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        DefinePlayer definePlayer = pm.getDefinePlayer(uuid);

        // This will null pointer if the player leaves the same tick as the game starts
        try {
            bukkitPlayer.setGameMode(GameMode.SURVIVAL);
            bukkitPlayer.setHealth(20);

            bukkitPlayer.setAllowFlight(allowPlayerDoubleJump);
        } catch (NullPointerException e) {
            MainAPI.getPlugin().getLogger().log(Level.WARNING, "Player left the same tick as the game started or as the player respawned!  Attempting to recover from this error!");
        }

        ItemStack[] giveItems;


        givePlayerKit(bukkitPlayer, spawnItemList);

        if (!(bukkitPlayer.getWorld().getName().equalsIgnoreCase("world"))) {
            Location teleportLocation = new Location(Bukkit.getWorld("world"), 0, 61, 0);

            PaperLib.teleportAsync(bukkitPlayer, teleportLocation).thenAccept(result -> {
                if (result) {
                    if (canMove == false) {
                        definePlayer.setFreeze(true);
                    }
                } else {
                    bukkitPlayer.sendMessage(ChatColor.RED + "Something went wrong while teleporting you to the game.  Recovering by sending you back to the hub");
                    Matchmaking mm = new Matchmaking();
                    mm.addPlayer(uuid, "lobby");
                }
            });
        }

        if (canMove == false) {
            definePlayer.setFreeze(true);
        }
    }

    @Override
    public void playerLoad(UUID uuid) {
        PlayerManager pm = new PlayerManager();

        DefinePlayer definePlayer = pm.getDefinePlayer(uuid);

        // This should be changed when this class becomes generic
        definePlayer.reset();
        definePlayer.setCanInfiniteRespawn(true);
        definePlayer.setInGameType(getGameType());

        Bukkit.getPlayer(uuid).addPotionEffect(
                new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 0, true, false));

        playerRespawn(uuid, true, true);
    }

    /*@Override
    public void playerJoin(UUID player) {

        try {
            uuidParticipating.add(player);

            Player bukkitPlayer = Bukkit.getPlayer(player);

            PaperLib.teleportAsync(bukkitPlayer, new Location(Bukkit.getWorld("world"), 0, 70, 0));

            bukkitPlayer.setGameMode(GameMode.SURVIVAL);
            bukkitPlayer.setHealth(20);
            bukkitPlayer.setFireTicks(0);
            bukkitPlayer.getInventory().clear();
            //PaperLib.teleportAsync(event.getPlayer(), new Location(Bukkit.getWorld("world"), 0, 70, 0));

            PlayerManager pm = new PlayerManager();
            pm.getDefinePlayer(player).setInGameType(getGameType());

            for (int i = 0; i < 41; i++) {
                bukkitPlayer.getInventory().setItem(i, spawnItemList[i]);
            }

            // TODO: Add option for queue.
            /*Matchmaking mm = new Matchmaking();
            new BukkitRunnable() {
                public void run() {
                    if (Bukkit.getOfflinePlayer(player).isOnline()) {
                        mm.addPlayer(player, "testwars");
                    }
                }
            }.runTaskLater(MainAPI.getPlugin(), 100L);
        } catch (Exception e) {
            playerLeave(player);
        }

    }*/

    @Override
    public void startGameCountdown() {

    }
}

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.logging.Level;

public class Lobby extends Game {
    private String nextGame;

    private boolean isStarting = false;


    @Override
    public void createGameWorldAndRegisterGame() {
        ConfigManager cm = new ConfigManager();

        // Set game type before so it loads the right configs
        setGameType("lobby");
        // Otherwise this is null, and that causes hours of debugging!
        setGameUUID(UUID.randomUUID());

        DefineWorld dwWorld = new DefineWorld();
        dwWorld.createArena("world", null);
        setGameWorld(dwWorld);

        setAllowPlayerMoveOnJoin(true);
        setAllowPlayerDoubleJump(true);
        setAllowGoldenLaunchpads(true);
        setAllowPlayersToJoinNow(true);
        setAlwaysAllowPlayersJoin(true);
        setAllowPlayerDamage(false);
        setAllowPlayerShootProjectile(false);
        setMaxPlayers(99999);

        GameManager gm = new GameManager();
        gm.registerGame(getGameUUID(), this);
        gm.registerWorld(getGameUUID(), dwWorld);

        spawnItemList = cm.getRandomKit(getGameType());

        setJoinServerLocation(new Location(Bukkit.getWorld("world"), 0, 61, 0));
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

        dp.setPlayerDefineTeam(getBestTeam());
        dp.setKit(getSpawnKitName());

        playerLoad(player);

        setScoreBoard(Bukkit.getPlayer(player));
    }

    /*public static void setScoreBoard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        // Registers title of scoreboard
        Objective obj = board.registerNewObjective("ServerName", "ServerName", ChatColor.AQUA + "AbyssMC");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        //Score blankTop = obj.getScore(ChatColor.BOLD + "");
        //blankTop.setScore(12);

        Score rank = obj.getScore(ChatColor.GREEN + "" + ChatColor.BOLD + "Rank"); // Gets the score of a fake player
        rank.setScore(9);

        Score rankValue = obj.getScore(ChatColor.WHITE + "Player");
        rankValue.setScore(8);

        Score blankAqua = obj.getScore(ChatColor.AQUA + "");
        blankAqua.setScore(7);

        Score onlineName = obj.getScore(ChatColor.AQUA + "" + ChatColor.BOLD + "Online");
        onlineName.setScore(6);

        Score onlineScore = obj.getScore( ChatColor.WHITE + "" + MainAPI.globalPlayers);
        onlineScore.setScore(5);

        Score blankGreen = obj.getScore(ChatColor.GREEN + "");
        blankGreen.setScore(4);

        Score rubies = obj.getScore(ChatColor.RED + "" + ChatColor.BOLD + "Rubies");
        rubies.setScore(3);

        Score rubiesValue = obj.getScore(ChatColor.WHITE + "0");
        rubiesValue.setScore(2);

        //Score blankRed = obj.getScore(ChatColor.RED + "");
        //blankRed.setScore(3);

        Score divider = obj.getScore(ChatColor.WHITE + "" + ChatColor.BOLD + "-------------");
        divider.setScore(1);

        Score serverName = obj.getScore(ChatColor.GREEN + "play.abyssmc.org");
        serverName.setScore(0);

        player.setScoreboard(board);
    }*/

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

    @Override
    public void playerRespawn(UUID uuid, boolean canMove, boolean kitSelector) {
        PlayerManager pm = new PlayerManager();
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

        givePlayerKit(bukkitPlayer, spawnItemList);

        if (!(bukkitPlayer.getWorld().getName().equalsIgnoreCase("world"))) {
            Location teleportLocation = new Location(Bukkit.getWorld("world"), 0, 70, 0);

            PaperLib.teleportAsync(bukkitPlayer, teleportLocation).thenAccept(result -> {
                if (result) {
                    if (canMove == false) {
                        definePlayer.setFreeze(true);
                    }
                } else {
                    bukkitPlayer.sendMessage(ChatColor.RED + "Something went wrong while teleporting you to the game.  Recovering by sending you back to the hub");
                    Matchmaking mm = new Matchmaking();
                    mm.addPlayerToCentralQueue(uuid, "lobby");
                }
            });
        }

        if (canMove == false) {
            definePlayer.setFreeze(true);
        }
    }

    @Override
    public void startGameCountdown() {

    }
}

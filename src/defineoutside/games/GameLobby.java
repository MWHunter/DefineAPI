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

import java.util.UUID;
import java.util.logging.Level;

public class GameLobby extends Game {
    private String lobbyForGametype = "";

    @Override
    public void createGameWorldAndRegisterGame() {
        ConfigManager cm = new ConfigManager();

        // Set game type before so it loads the right configs
        setGameType("gamelobby");
        super.createGameWorldAndRegisterGame();

        DefineWorld dwWorld = new DefineWorld();
        dwWorld.createArena("world", null);
        setGameWorld(dwWorld);

        setGameCountdown(5);
        setGameCountdownDecrement(10);
        setAllowPlayerMoveOnJoin(true);
        setAllowPlayersToJoinNow(true);
        setAllowPlayerDamage(false);
        setAllowPlayerShootProjectile(false);
        super.createGameWorldAndRegisterGame();

        GameManager gm = new GameManager();
        gm.registerGame(getGameUUID(), this);
        gm.registerWorld(getGameUUID(), dwWorld);

        setJoinServerLocation(new Location(Bukkit.getWorld("world"), 0, 70, 0));
    }

    @Override
    public void start() {
        if (getUuidParticipating().size() < GameManager.getMinPlayers(getLobbyForGametype())) {
            GameManager gm = new GameManager();

            Game game;

            game = gm.createLocalGame(getLobbyForGametype());

            //Bukkit.broadcastMessage("The gamelobby is for gametype and is about to start " + getLobbyForGametype());

            if (game != null) {
                gm.transferPlayers(getUuidParticipating(), game);

                setCanGameStart(true);
                setGameStarting(false);
                setLobbyForGametype("");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "Server start failed!  Unknown gamemode: " + getLobbyForGametype());
                setCanGameStart(false);
                setGameStarting(false);
                setLobbyForGametype("");

                attemptStart();
            }
        } else {
            messageGamePlayers(ChatColor.RED + "Unable to start game because a player left before it could begin!");
            setCanGameStart(true);
            setGameStarting(false);
        }
    }

    @Override
    public void doEndCountdown() {
        /*if (!isGameEnding) {
            isGameEnding = true;

            // No players, or everyone is one one team.  End the game after the countdown
            new BukkitRunnable() {
                public void run() {
                    end();
                }
            }.runTaskLater(MainAPI.getPlugin(), getGameEndCountdown() * 20L);
        }*/
    }

    public String getLobbyForGametype() {
        return lobbyForGametype;
    }

    public void setLobbyForGametype(String lobbyForGametype) {
        GameManager gm = new GameManager();
        this.lobbyForGametype = lobbyForGametype;
    }

    public void attemptStart() {
        //Bukkit.broadcastMessage(uuidParticipating.size() + " is current playercount min is " + minPlayers + " " + !isGameStarting + " " + canGameStart);
        if (uuidParticipating.size() >= GameManager.getMinPlayers(getLobbyForGametype()) && !getGameStarting() && getCanGameStart()) {
            startGameCountdown();
        }
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
    public void playerLeave(Player player) {
        playerLeave(player.getUniqueId());

        if (getUuidParticipating().size() < GameManager.getMinPlayers(getLobbyForGametype())) {

        }
    }
}

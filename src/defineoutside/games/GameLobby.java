package defineoutside.games;

import defineoutside.creator.DefinePlayer;
import defineoutside.creator.DefineTeam;
import defineoutside.creator.DefineWorld;
import defineoutside.creator.Game;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import defineoutside.main.Matchmaking;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

public class GameLobby extends Game {
    private String lobbyForGametype = "";

    private boolean cancelStart = false;

    @Override
    public void createGameWorldAndRegisterGame() {

        // Set game type before so it loads the right configs
        setGameType("gamelobby");
        // Default the gametype to bedwars
        //setLobbyForGametype("bedwars");

        super.createGameWorldAndRegisterGame();

        DefineWorld dwWorld = new DefineWorld();
        dwWorld.createArena("world", null);
        setGameWorld(dwWorld);

        setGameCountdown(10);
        setGameCountdownDecrement(10);
        setAllowPlayerMoveOnJoin(true);
        setAllowPlayersToJoinNow(true);
        setAllowPlayerDamage(false);
        setAllowPlayerShootProjectile(false);

        GameManager.registerGame(getGameUUID(), this);
        GameManager.registerWorld(getGameUUID(), dwWorld);

        setJoinServerLocation(new Location(Bukkit.getWorld("world"), 0, 70, 0));
    }

    @Override
    public void start() {
        if (uuidParticipating.size() >= GameManager.getMinPlayers(getLobbyForGametype())) {
            Game game = GameManager.createLocalGame(getLobbyForGametype());

            if (game != null) {
                // Figure out best number of teams
                int maxTeams = 4;
                double minTeamDifferenceTimes = Double.MAX_VALUE;
                int bestNumberOfTeams = maxTeams;
                int numberOfPlayers = uuidParticipating.size();

                for (int teamCount = maxTeams; teamCount >= 2; teamCount--) {

                    int playerExtras = numberOfPlayers % teamCount;
                    int playersMinOnTeam = numberOfPlayers / teamCount;

                    double currentExtraMultiplier;
                    if (playerExtras != 0) {
                        currentExtraMultiplier = 1D / (double)playersMinOnTeam;
                    } else {
                        currentExtraMultiplier = 0;
                    }

                    if (currentExtraMultiplier <= minTeamDifferenceTimes && minTeamDifferenceTimes >= 1) {
                        bestNumberOfTeams = teamCount;
                        minTeamDifferenceTimes = currentExtraMultiplier;
                    }
                }

                game.setTeams(bestNumberOfTeams);
                game.setGenerateTeams(true);
                game.createTeams();

                // Shuffle the list
                Collections.shuffle(uuidParticipating);
                ArrayList<DefineTeam> newTeams = game.uuidDefineTeams;

                // We have put the best number of teams in the variable... bestNumberOfTeams
                // Time to place everyone into teams!
                for (DefinePlayer definePlayer : uuidParticipating) {
                    definePlayer.setPlayerDefineTeam(newTeams.get(uuidParticipating.indexOf(definePlayer) % bestNumberOfTeams));
                    newTeams.get(uuidParticipating.indexOf(definePlayer) % bestNumberOfTeams).addPlayer(definePlayer);
                }

                game.setUuidDefineTeams(newTeams);

                // Transfer players
                GameManager.transferPlayers(getUuidParticipating(), game);

                // Reset the lobby
                setCanGameStart(true);
                setGameStarting(false);
                setLobbyForGametype("");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "Unknown gamemode: \"" + getLobbyForGametype() + "\"  Starting bedwars!");
                //Bukkit.broadcastMessage(ChatColor.RED + "Defaulting to the bedwars gamemode and starting!");
                setCanGameStart(false);
                setGameStarting(false);
                setLobbyForGametype("bedwars");

                start();
            }
        } else {
            messageGamePlayers(ChatColor.RED + "Unable to start game because a player left before it could begin!  Will begin when more players join!");
            setCanGameStart(true);
            setGameStarting(false);
        }
    }

    @Override
    public void playerLeave(DefinePlayer player) {
        uuidParticipating.remove(player);
        player.getPlayerDefineTeam().removePlayer(player);

        if (uuidParticipating.size() < GameManager.getMinPlayers(getLobbyForGametype())) {
            isGameEnding = true;
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
        //Bukkit.broadcastMessage(uuidParticipating.size() + " VS min " + GameManager.getMinPlayers(getGameType()));
        if (uuidParticipating.size() >= GameManager.getMinPlayers(getLobbyForGametype()) && !getGameStarting() && getCanGameStart()) {
            startGameCountdown();
        }
    }

    @Override
    public void playerRespawn(DefinePlayer definePlayer, boolean canMove, boolean kitSelector) {
        Player bukkitPlayer = definePlayer.getBukkitPlayer();
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
                    mm.addPlayerToCentralQueue(definePlayer, "lobby");
                }
            });
        }

        if (canMove == false) {
            definePlayer.setFreeze(true);
        }
    }
}
